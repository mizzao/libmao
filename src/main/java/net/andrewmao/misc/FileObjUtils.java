package net.andrewmao.misc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileObjUtils {

	/**
	 * 
	 * @param <T>
	 * @param filename
	 * @param spd
	 */
	public static <T> void writeToGZIP(String filename, T obj) {
		ObjectOutputStream os = null;
		
		try {
			os = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(filename)));
			os.writeObject(obj);					
			System.out.println("Object written to " + filename);
			
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
			
		} catch (IOException e) {			
			e.printStackTrace();
			
		} finally {
			try {
				if (os != null) { os.flush(); os.close(); }
			} catch (IOException e) {			
				e.printStackTrace();
			}
		}		
	}

	/**
	 * 
	 * @param <T>
	 * @param filename
	 * @param spd A generically typed value to allow the type cast to work correctly, usually set to null.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T readFromGZIP(String filename, T obj) {		
		ObjectInputStream ois = null;		
		
		try {
			ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(filename)));			
			obj = (T) ois.readObject();			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
		} catch (IOException e) {			
			e.printStackTrace();
			
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
			
		} finally {
	
			try {
				if( ois != null ) ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return obj;
	}

	public static void writeToFile(String filename, String str) {
		FileWriter fw = null;
		
		try {
			fw = new FileWriter(filename);
			fw.write(str);
		} catch( IOException e ) {
			e.printStackTrace();
		} finally {
			try {
				if( fw != null ) { fw.flush(); fw.close(); }
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
