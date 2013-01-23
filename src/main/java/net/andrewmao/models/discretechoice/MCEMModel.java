package net.andrewmao.models.discretechoice;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public abstract class MCEMModel<M extends NoiseModel<?>> extends RandomUtilityEstimator<M> {
	
	final int maxThreads;
	
	int maxIter;
	double abseps;
	double releps;
	double[] start;
	
	ExecutorService exec;
	CountDownLatch latch;
	
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
	protected abstract void mStep();
	protected abstract double[] getCurrentParameters();
	protected abstract double getLogLikelihood();
		
	@Override
	public double[] getParameters(List<int[]> rankings, int numItems) {
		/*
		 * NOT reentrant. Don't call this from multiple threads.
		 */
		
		exec = Executors.newFixedThreadPool(maxThreads);
		
		initialize(rankings, numItems);
//		double ll = Double.NEGATIVE_INFINITY;
		double absImpr = Double.POSITIVE_INFINITY;
		
		RealVector oldParams = null, params = null;
		
		for( int i = 0; i < maxIter; i++ ) {
			eStep(i);
			
			// Wait for sampling to finish
			while( latch.getCount() > 0 ) {
				try { latch.await();
				} catch (InterruptedException e) {}				
			}
			
			mStep();
						
			params = new ArrayRealVector(getCurrentParameters());
			if( i > 0 )
				absImpr = params.subtract(oldParams).getNorm();			
			
//			double newLL = getLogLikelihood();
//			System.out.printf("Likelihood: %f\n", newLL);
//			double absImpr = newLL - ll;
//			double relImpr = -absImpr / ll;
			
			if( absImpr < abseps ) {
//				System.out.printf("Absolute tolerance reached: %f < %f\n", absImpr, abseps);
				break;
			}
//			if( relImpr < releps ) {
////				System.out.printf("Relative tolerance reached: %f < %f\n", relImpr, releps);
//				break;
//			}
			
			oldParams = params;
//			ll = newLL;
		}
		
		exec.shutdown();
		
		return (params.toArray());
	}

	void beginNumJobs(int size) {
		latch = new CountDownLatch(size);	
	}

	void addJob(Callable<?> job) {
		exec.submit(job);		
	}

	void finishJob() {
		latch.countDown();
	}
}
