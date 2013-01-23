package net.andrewmao.models.noise;

import static org.junit.Assert.*;

import java.util.Random;

import net.andrewmao.math.RandomSelection;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NormalLogLikelihoodTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test3xLikelihood() {
		int n = 3;
		int trials = 1000;
		double tol = 1e-5;		
		
		RealVector mean = new ArrayRealVector(3);
		RealVector variance = new ArrayRealVector(3);		
		NormalLogLikelihood ll = new NormalLogLikelihood(mean, variance);
				
		int[] arr = new int[] {1, 2, 3};
		Random rnd = new Random();
		
		for( int i = 0; i < trials; i++ ) {
			for( int j = 0; j < n; j++ ) {
				mean.setEntry(j, rnd.nextDouble() * 2 - 1);
				variance.setEntry(j, rnd.nextDouble() * 2);
			}
			RandomSelection.shuffle(arr, rnd);			
			
			double bivariateLL = ll.bivariateLL(arr);
			double multivariateLL = ll.multivariateLL(arr);
			
			assertEquals(bivariateLL, multivariateLL, tol);
		}		
	}

}
