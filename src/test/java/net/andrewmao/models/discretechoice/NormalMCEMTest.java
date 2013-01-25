package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.andrewmao.models.discretechoice.OrderedNormalMCEM;
import net.andrewmao.models.noise.NormalNoiseModel;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NormalMCEMTest {
	
	static Character[] stuff = new Character[] { 'A', 'B', 'C', 'D' };
	static final List<Character> stuffList = Arrays.asList(stuff);	
	
	Random rnd;
	
	@Before
	public void setUp() throws Exception {
		rnd = new Random();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testSpeed() {
		int trials = 10;
		int n = 10;		
		int iters = 30;
		
		double abseps = 1e-3; // Double.NEGATIVE_INFINITY;
		double releps = 1e-3; // Double.NEGATIVE_INFINITY;					
		
		OrderedNormalMCEM model = new OrderedNormalMCEM(true, iters, abseps, releps);
		NormalNoiseModel<Character> gen = new NormalNoiseModel<Character>(stuffList, rnd, 1, 1);
		
		long startTime = System.currentTimeMillis();
		
		for( int i = 0; i < trials; i++ ) {
			PreferenceProfile<Character> prefs = gen.sampleProfile(n);			
			model.setup(new NormalDistribution(0,1).sample(4));			
			ScoredItems<Character> fitted = model.fitModel(prefs).getValueMap();			
			System.out.println(fitted);	
		}		
		
		long stopTime = System.currentTimeMillis();
		
		double avgTime = (stopTime - startTime) / trials;
		System.out.printf("Avg time for 10x4 preference profiles: %.0f ms\n", avgTime);		
	}

	@Test
	public void testInference() {
		int n = 10000;		
		int iters = 50;
		double abseps = 1e-5; // Double.NEGATIVE_INFINITY;
		double releps = 1e-5; // Double.NEGATIVE_INFINITY;
		
		double[] means = new double[] {0, -1, -2, -3};
				
		OrderedNormalMCEM model = new OrderedNormalMCEM(true, iters, abseps, releps);		
		NormalNoiseModel<Character> gen = new NormalNoiseModel<Character>(stuffList, rnd, means, 1);
		
		PreferenceProfile<Character> prefs = gen.sampleProfile(n);
		
		model.setup(new NormalDistribution(0,1).sample(4));
		
		ScoredItems<Character> fitted = model.fitModel(prefs).getValueMap();
		
		System.out.println(fitted);
		
		// This assertion must have first element of means be 0, and variances adjusted to 1
		assertArrayEquals(means, fitted.toArray(), 1e-1);
	}
	
}
