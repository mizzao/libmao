package net.andrewmao.models.noise;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LinearNormalTest {

	Character[] ls = new Character[] { 'a', 'b', 'c', 'd' };
	List<Character> letters = Arrays.asList(ls);
	
	int n = 10;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {			
		int m = letters.size();
		
		double a_mean = 1000;		
		double lambda = 0.000001;
		double stdev = 1d/Math.sqrt(lambda);
		
		double[] means = new double[] {0, 1, 2, 3};
		double scale = .5;
		for( int i = 0; i < means.length; i++ ) means[i] *= scale;
		
		NormalDistribution sampler = new NormalDistribution(a_mean, stdev);
		
		NormalNoiseModel<Character> generator = 
				new NormalNoiseModel<Character>(letters, means, 1);
		Random rnd = new Random();
		
		double[][] scores = new double[n][];
		
		double[] a = new double[n];
		
		for( int j = 0; j < n; j++ ) {
			a[j] = Math.max(0, sampler.sample());
			
			double[] generated = generator.sampleUtilities(rnd);
			for( int i = 0; i < m; i++ ) {
				generated[i] = a[j] * generated[i];
			}
			scores[j] = generated;
		}
		
		LinearNormalEstimator est = new LinearNormalEstimator(new Sum().evaluate(means), 1e-6);
		NormalNoiseModel<Character> estimated = est.fitModelCardinal(letters, scores);
	
		double[] a_estimated = est.a_last;
		for( int j = 0; j < n; j++ ) {
			System.out.printf("%d: %.04f %.04f\n", j, a[j], a_estimated[j]);
		}
		
		System.out.println("Original deltas: " + generator.toParamString());
		System.out.println("Estimated deltas: " + estimated.toParamString());
		
		System.out.println("Actual mean of a: " + new Mean().evaluate(a));
		System.out.println("Estimated mean of a: " + new Mean().evaluate(a_estimated));
		
		System.out.println("Actual stdev of a: " + new StandardDeviation().evaluate(a));
		System.out.println("Estimated stdev of a: " + new StandardDeviation().evaluate(a_estimated));
		
		fail("Not yet implemented");
	}

}
