package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Random;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TMNLogLikelihoodTest {

	int trials = 100;
	double small_err = 1e-12;
	double eps = 1e-8;
	double tol = 1e-4;	
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGradient() {
		int max = 20;
		int n = 10;
		
		Random rnd = new Random();
		
		// Create random matrix
		RealMatrix mat = new Array2DRowRealMatrix(n, n);				
		for( int i = 0; i < n; i++ ) 
			for( int j = 0; j < n; j++)
				mat.setEntry(i, j, rnd.nextInt(max));
		
		TMNLogLikelihood tmnll = new TMNLogLikelihood(mat);
		
		MultivariateVectorFunction g = tmnll.gradient();		
		MultivariateFunction[] partials = new MultivariateFunction[n-1];
		for( int k = 0; k < n-1; k++ ) {
			partials[k] = tmnll.partialDerivative(k);
		}
		
		for( int i = 0; i < trials; i++ ) {
			double[] thetas = new double[n-1];
			for( int k = 0; k < n-1; k++ ) 
				thetas[k] = 2 * rnd.nextDouble() - 1;
			
			double[] gradient = g.value(thetas);
			
			for( int k = 0; k < n-1; k++ ) {
				double orig = thetas[k];
				
				thetas[k] = orig + eps;				
				double over = tmnll.value(thetas);
				
				thetas[k] = orig - eps;
				double under = tmnll.value(thetas);
				
				thetas[k] = orig;
				
				// Check gradient with linear approximation
				assertEquals(gradient[k], (over - under)/(2*eps), tol);
								
				// Check gradient with partial
				assertEquals(gradient[k], partials[k].value(thetas), small_err);
			}
		}
	}
}
