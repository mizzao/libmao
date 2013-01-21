package net.andrewmao.socialchoice.preferences;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import net.andrewmao.models.discretechoice.PlackettLuceModel;
import net.andrewmao.models.discretechoice.ScoredItems;
import net.andrewmao.socialchoice.preferences.GumbelNoiseGenerator;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class GumbelNoiseGeneratorTest {

	Character[] ls = new Character[] { 'a', 'b', 'c', 'd' };
	List<Character> letters = Arrays.asList(ls);
	
	Comparator<Character> comp = new Comparator<Character>() {
		@Override
		public int compare(Character o1, Character o2) {			
			return new Character(o1).compareTo(o2);
		}		
	};
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

			
	@Test
	public void testExpo() {
		System.out.println("Testing Exponential...");
		
		int n = 100000;
		
		SummaryStatistics m = new SummaryStatistics();
		double mean = 2;
		ExponentialDistribution expo = new ExponentialDistribution(mean);
		
		for( int i = 0; i < n; i++ )
			m.addValue(expo.sample());
		
		double resultMean = m.getMean();
		double resultVar = m.getVariance();
		
		System.out.println("Mean: " + mean);
		System.out.println("Sampled: " + resultMean);
		assertEquals(mean, resultMean, 1e-2);
		
		System.out.println("Variance: " + mean * mean);
		System.out.println("Sampled: " + resultVar);
		assertEquals(mean * mean, resultVar, 1e-1);
	}
	
	@Test
	public void testProbabilities() {
		System.out.println("Testing pairwise probabilities");
		
		int size = 100000;
		double tol = 1e-2;
		double strDiff = 0.2;				
		
		GumbelNoiseGenerator<Character> gen = new GumbelNoiseGenerator<Character>(letters, new Random(), strDiff);
						
		PreferenceProfile<Character> prefs = gen.getRandomProfile(size);	
		
		System.out.println(1.0 * prefs.getNumCorrect('a', 'b', comp) / size);
		System.out.println(1/(1+Math.exp(-strDiff)));
		
		System.out.println(1.0 * prefs.getNumCorrect('a', 'c', comp) / size);
		System.out.println(1/(1+Math.exp(-2*strDiff)));
		
		System.out.println(1.0 * prefs.getNumCorrect('a', 'd', comp) / size);
		System.out.println(1/(1+Math.exp(-3*strDiff)));
				
		assertEquals(1/(1+Math.exp(-strDiff)), 1.0 * prefs.getNumCorrect('a', 'b', comp) / size, tol);
		assertEquals(1/(1+Math.exp(-strDiff)), 1.0 * prefs.getNumCorrect('b', 'c', comp) / size, tol);
		assertEquals(1/(1+Math.exp(-strDiff)), 1.0 * prefs.getNumCorrect('c', 'd', comp) / size, tol);
		
		assertEquals(1/(1+Math.exp(-2*strDiff)), 1.0 * prefs.getNumCorrect('a', 'c', comp) / size, tol);
		assertEquals(1/(1+Math.exp(-2*strDiff)), 1.0 * prefs.getNumCorrect('b', 'd', comp) / size, tol);
		
		assertEquals(1/(1+Math.exp(-3*strDiff)), 1.0 * prefs.getNumCorrect('a', 'd', comp) / size, tol);		
	}

	/*
	 * Test that the Plackett-Luce model recovers the right parameter differences under the distribution
	 */
	@Test
	public void testPlackettLuce() {
		int trials = 10;
		int size = 100000;
		double tol = 0.02;
		
		for( int a = 0; a < trials; a++ ) {
			double strDiff = Math.random();

			GumbelNoiseGenerator<Character> gen = new GumbelNoiseGenerator<Character>(letters, new Random(), strDiff);

			PreferenceProfile<Character> prefs = gen.getRandomProfile(size);	

			PlackettLuceModel<Character> plmm = new PlackettLuceModel<Character>(letters);

			for( Character[] ranking : prefs.getProfile() )
				plmm.addData(ranking);

			ScoredItems<Character> params = plmm.getParameters(); 
			System.out.println(params);

			for( int i = 0; i < ls.length; i++ ) {
				assertEquals(-i*strDiff, params.get(ls[i]).doubleValue(), tol);
			}
		}		
	}

}
