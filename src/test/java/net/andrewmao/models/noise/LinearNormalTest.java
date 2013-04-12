package net.andrewmao.models.noise;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LinearNormalTest {

	Character[] ls = new Character[] { 'a', 'b', 'c', 'd' };
	List<Character> letters = Arrays.asList(ls);
	
	int n = 100;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {			
		int m = letters.size();
		double lambda = .01;
		double[] means = new double[] {0, 0.5, 1.0, 1.5};		
		ExponentialDistribution expo = new ExponentialDistribution(1d/lambda);
		
		NormalNoiseModel<Character> generator = 
				new NormalNoiseModel<Character>(letters, means, 1);
		Random rnd = new Random();
		
		double[][] scores = new double[n][];
		
		double[] a = new double[n];
		
		for( int j = 0; j < n; j++ ) {
			a[j] = expo.sample();
			double[] generated = generator.sampleUtilities(rnd);
			for( int i = 0; i < m; i++ ) {
				generated[i] = a[j] * generated[i];
			}
			scores[j] = generated;
		}
		
		LinearNormalEstimator est = new LinearNormalEstimator(lambda, 1e-6);
		NormalNoiseModel<Character> estimated = est.fitModelCardinal(letters, scores);
	
		double[] a_estimated = est.a_last;
		for( int j = 0; j < n; j++ ) {
			System.out.printf("%d: %.04f %.04f\n", j, a[j], a_estimated[j]);
		}
		
		System.out.println("Original deltas: " + generator.toParamString());
		System.out.println("Estimated deltas: " + estimated.toParamString());
		
		fail("Not yet implemented");
	}

}
