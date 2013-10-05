package net.andrewmao.probability;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MultivariateNormalThreadedTest {

	static final int threads = Runtime.getRuntime().availableProcessors();
	static final int trials = 2000;
	static final Random rnd = new Random();
	
	final MultivariateNormal mvn = MultivariateNormal.DEFAULT_INSTANCE;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		CDFTest task = new CDFTest();
		
		List<CDFTest> tasks = Collections.nCopies(threads, task);
	    ExecutorService executorService = Executors.newFixedThreadPool(threads);
	    List<Future<Void>> futures = executorService.invokeAll(tasks);	    
	    
	    // Check for exceptions
	    for (Future<Void> future : futures) {
	        // Throws an exception if an exception was thrown by the task.
	        future.get();
	    }
	}
	
	class CDFTest implements Callable<Void> {
		@Override
		public Void call() {
			int n = 4;
			
			RealVector mean = new ArrayRealVector(n, 0);
			RealMatrix sigma = new Array2DRowRealMatrix(n, n);
			
			sigma.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
				@Override
				public double visit(int row, int column, double value) {				
					return row == column ? 1 : 0;
				}			
			});
			
			double[] lower = new double[n];
			
			double[] upper = new double[n];
			for( int i = 0; i < upper.length; i++ )
				upper[i] = Double.POSITIVE_INFINITY;
			
			double expected = Math.pow(0.5, 4); 
			
			for( int i = 0; i < trials; i++ ) {				
				double value = mvn.cdf(mean, sigma, lower, upper).cdf;				
				assertEquals(expected, value, mvn.cdf_abseps);	
			}	
			
			return null;
		}		
	}
	
	class ExpTest implements Callable<Void> {
		@Override
		public Void call() {
			int n = 4;		
			
			RealVector mean = new ArrayRealVector(n, 0);
			RealMatrix sigma = new Array2DRowRealMatrix(n, n);
			
			sigma.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
				@Override
				public double visit(int row, int column, double value) {				
					return row == column ? 1 : 0;
				}			
			});
			
			double[] lower = new double[n];
			
			double[] upper = new double[n];
			for( int i = 0; i < upper.length; i++ )
				upper[i] = Double.POSITIVE_INFINITY;
			
			for( int i = 0; i < trials; i++ ) {
				double[] values = mvn.exp(mean, sigma, lower, upper).expValues;							
				
				for( double d : values )
					assertEquals(Math.sqrt(2.0/Math.PI), d, mvn.exp_releps);	
			}			
			
			return null;
		}
	}

}
