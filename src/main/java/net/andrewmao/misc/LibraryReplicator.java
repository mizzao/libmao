package net.andrewmao.misc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.io.Files;
import com.sun.jna.Native;

/**
 * Class to make a non-reentrant native library threadsafe 
 * by copying it and maintaining a queue of locked objects 
 * 
 * @author mao
 *
 */
public class LibraryReplicator<C> {

	final BlockingQueue<C> libQueue;
	final Class<C> interfaceClass;
	final C proxiedInterface;
	
	@SuppressWarnings("unchecked")
	public LibraryReplicator(URL libraryResource, Class<C> interfaceClass, int copies) throws IOException {
		if (!interfaceClass.isInterface()) 
			throw new RuntimeException(interfaceClass + "is not a valid interface to map to the library.");
				
		libQueue = new LinkedBlockingQueue<C>(copies);
		this.interfaceClass = interfaceClass;
		
		// Create copies of the file and map them to interfaces
		String orig = libraryResource.getFile();
		File origFile = new File(orig);
		for( int i = 0; i < copies; i++ ) {
			File copy = new File(orig + "." + i);
			Files.copy(origFile, copy);
						
			C libCopy = (C) Native.loadLibrary(copy.getPath(), interfaceClass);
			libQueue.offer(libCopy); // This should never fail
		}
		
		proxiedInterface = (C) Proxy.newProxyInstance(
				interfaceClass.getClassLoader(), 
				new Class[] { interfaceClass }, 
				new BlockingInvocationHandler());
	}
	
	public LibraryReplicator(URL libraryResource, Class<C> interfaceClass) throws IOException {
		this(libraryResource, interfaceClass, Runtime.getRuntime().availableProcessors());
	}
	
	public C getProxiedInterface() {
		return proxiedInterface;
	}
	
	/*
	 * Invocation handler that uses the queue to grab locks and maintain thread safety.	 
	 */
	private class BlockingInvocationHandler implements InvocationHandler {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
			C instance = null;
			// Grab a copy of the library out of the queue
			do {
				try { instance = libQueue.take(); }
				catch(InterruptedException e) {}
			} while(instance == null);
			
			// Invoke the method
			Object result = method.invoke(instance, args);
			
			// Return the library to the queue
			while(true) {
				try { libQueue.put(instance); break; }
				catch( InterruptedException e ) {} 
			} 
			
			return result;
		}		
	}
	
}
