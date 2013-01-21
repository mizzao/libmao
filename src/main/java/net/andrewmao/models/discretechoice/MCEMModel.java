package net.andrewmao.models.discretechoice;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public abstract class MCEMModel<T> extends RandomUtilityModel<T> {
			
	int maxIter;
	double abseps;
	double releps;
	double[] start;
	
	ExecutorService exec;
	CountDownLatch latch;
	
	protected MCEMModel(List<T> items) {
		super(items);						
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
	protected abstract void initialize();	
	protected abstract void eStep(int iter);	
	protected abstract void mStep();
	protected abstract double[] getCurrentParameters();
	protected abstract double getLogLikelihood();
	
	@Override
	public synchronized ScoredItems<T> getParameters() {
		exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		initialize();
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
		
		return new ScoredItems<T>(items, params.toArray());
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
