package net.andrewmao.models.discretechoice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EstimatorUtils {
	
	// Make all parallelized EM models share the same threadpool.
	static final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
}
