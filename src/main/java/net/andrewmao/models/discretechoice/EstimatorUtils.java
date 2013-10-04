package net.andrewmao.models.discretechoice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EstimatorUtils {
	
	// Make all parallelized EM models share the same threadpool.
	static final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	// Shut this threadpool down on exit
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				threadPool.shutdown();
			}
		});
	}
	
	public static void shutdown() {
		threadPool.shutdown();
	}
	
}
