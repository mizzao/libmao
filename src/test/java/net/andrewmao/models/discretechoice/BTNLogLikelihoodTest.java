package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Random;

import org.apache.commons.math3.analysis.DifferentiableMultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BTNLogLikelihoodTest {

	double eps = 1e-8;
	double small_err = 1e-12;
	int trials = 100;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testOneWin() {
		int[][] wins = new int[2][2];
		
		wins[0][1] = 1;
		wins[1][0] = 0;
		
		DifferentiableMultivariateFunction btnll = new BTNLogLikelihood(wins);
		
		for( int i = 0; i < trials; i++ ) {
			double theta_0 = 0;
			double theta_1 = 2 * Math.random() - 1;
			
			double expected = 1.0/(1+Math.exp(theta_1 - theta_0));
			double nlogExpected = -Math.log(expected);
			
			assertEquals(nlogExpected, btnll.value(new double[] { theta_1 }), small_err);
		}				 
	}
	
	@Test
	public void testCompound() {
		int[][] wins = new int[3][3];
		
		wins[0][1] = 1;
		wins[1][2] = 1;
		
		DifferentiableMultivariateFunction btnll = new BTNLogLikelihood(wins);
		
		for( int i = 0; i < trials; i++ ) {
			double theta_0 = 0;
			double theta_1 = 2 * Math.random() - 1;
			double theta_2 = 2 * Math.random() - 1; 
			
			double expected = 1.0/(1+Math.exp(theta_1 - theta_0))/(1+Math.exp(theta_2 - theta_1));
			double nlogExpected = -Math.log(expected);
			
			assertEquals(nlogExpected, btnll.value(new double[] { theta_1, theta_2 }), small_err);
		}
	}
	
	@Test
	public void testGradient() {
		int max = 20;
		int n = 10;
		
		Random rnd = new Random();
		
		int[][] wins = new int[n][n];
		for( int i = 0; i < n; i++ ) 
			for( int j = 0; j < n; j++)
				wins[i][j] = rnd.nextInt(max);
		
		DifferentiableMultivariateFunction btnll = new BTNLogLikelihood(wins);
		
		MultivariateVectorFunction g = btnll.gradient();		
		
		for( int i = 0; i < trials; i++ ) {
			double[] thetas = new double[n-1];
			for( int k = 0; k < n-1; k++ ) 
				thetas[k] = 2 * rnd.nextDouble() - 1;
			
			double[] gradient = g.value(thetas);
			
			for( int k = 0; k < n-1; k++ ) {
				double orig = thetas[k];
				thetas[k] = orig + eps;				
				double over = btnll.value(thetas);
				thetas[k] = orig - eps;
				double under = btnll.value(thetas);		
				thetas[k] = orig;
				
				assertEquals(gradient[k], (over - under)/(2*eps), 1e-4);
			}
		}
	}
	
	@Test
	public void testGradientPartial() {
		int max = 20;
		int n = 10;
		
		Random rnd = new Random();
		
		int[][] wins = new int[n][n];
		for( int i = 0; i < n; i++ ) 
			for( int j = 0; j < n; j++)
				wins[i][j] = rnd.nextInt(max);
		
		DifferentiableMultivariateFunction btnll = new BTNLogLikelihood(wins);
		
		MultivariateVectorFunction g = btnll.gradient();
		MultivariateFunction[] partials = new MultivariateFunction[n-1];
		for( int k = 0; k < n-1; k++ ) {
			partials[k] = btnll.partialDerivative(k);
		}
		
		for( int i = 0; i < trials; i++ ) {
			double[] thetas = new double[n-1];
			for( int k = 0; k < n-1; k++ ) 
				thetas[k] = 2 * rnd.nextDouble() - 1;
			
			double[] gradient = g.value(thetas);
			for( int k = 0; k < n-1; k++ )
				assertEquals(gradient[k], partials[k].value(thetas), small_err);
		}
	}

}
