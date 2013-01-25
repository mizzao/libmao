package net.andrewmao.models.discretechoice;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import net.andrewmao.models.noise.NoiseModel;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * Implementation of Azari, Parkes, Xia paper on RUM 
 * via MC-EM, using multithreading
 * 
 * @author mao
 *
 * @param <T>
 */
public abstract class MCEMModel<T, M extends NoiseModel<?>> extends RandomUtilityEstimator<M> {
	
	final int maxThreads;
	
	int maxIter;
	double abseps;
	double releps;
	double[] start;
	
	ExecutorService exec;
	AtomicInteger submittedJobs;
	CompletionService<T> ecs;
	
	public MCEMModel(int maxThreads) {
		this.maxThreads = Math.min(maxThreads, Runtime.getRuntime().availableProcessors());
	}
	
	public MCEMModel() {
		this(Runtime.getRuntime().availableProcessors());
	}
	
	public void setup(double[] startPoint, int maxIter, double abseps, double releps) {
		this.maxIter = maxIter;
		this.abseps = abseps;
		this.releps = releps;
		this.start = startPoint;
	}
	
	/*
	 * Implemented by subclasses
	 */
	protected abstract void initialize(List<int[]> rankings, int numItems);	
	protected abstract void eStep(int iter);
	protected abstract void addData(T data);
	protected abstract void mStep();
	protected abstract double[] getCurrentParameters();
	protected abstract double getLogLikelihood();
		
	@Override
	public synchronized double[] getParameters(List<int[]> rankings, int numItems) {
		/*
		 * NOT reentrant. Don't call this from multiple threads.
		 */		
		
		exec = Executors.newFixedThreadPool(12);
		
		ecs = new ExecutorCompletionService<T>(exec);
		submittedJobs = new AtomicInteger(0);
		
		initialize(rankings, numItems);
		double ll = Double.NEGATIVE_INFINITY;
//		double absImpr = Double.POSITIVE_INFINITY;
		
//		RealVector oldParams = null; 
		RealVector params = null;
		
		for( int i = 0; i < maxIter; i++ ) {
			submittedJobs.set(0);
			
			eStep(i);						
			
			// Wait for sampling to finish
			int jobs = submittedJobs.get();
			
			for( int j = 0; j < jobs; j++ ) {
				try {
					addData(ecs.take().get());
				} catch (InterruptedException e) {					
					e.printStackTrace();
					j--;
				} catch (ExecutionException e) {					
					e.getCause().printStackTrace();					
				}
			}						
									
			mStep();			
						
			params = new ArrayRealVector(getCurrentParameters());
//			if( i > 0 )
//				absImpr = params.subtract(oldParams).getNorm();			
						
			double newLL = getLogLikelihood();
//			System.out.printf("Likelihood: %f\n", newLL);
			double absImpr = newLL - ll;
			double relImpr = -absImpr / ll;
			
			if( absImpr < abseps ) {
				System.out.printf("Absolute tolerance reached: %f < %f\n", absImpr, abseps);
				break;
			}
			if( relImpr < releps ) {
				System.out.printf("Relative tolerance reached: %f < %f\n", relImpr, releps);
				break;
			}
			
//			oldParams = params;
			ll = newLL;
		}
				
		exec.shutdown();
		
		return (params.toArray());
	}

	void addJob(Callable<T> job) {
		submittedJobs.incrementAndGet();
		ecs.submit(job);
	}

}
