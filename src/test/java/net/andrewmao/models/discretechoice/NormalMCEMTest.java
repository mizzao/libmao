package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.andrewmao.models.discretechoice.OrderedNormalMCEM;
import net.andrewmao.models.noise.NormalNoiseModel;
import net.andrewmao.models.noise.TestParameterGen;
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
	public void testSpeedFixed() {
		int trials = 10;
		int n = 10;		
		int iters = 30;
		
		double abseps = 1e-3; // Double.NEGATIVE_INFINITY;
		double releps = 1e-3; // Double.NEGATIVE_INFINITY;					
		
		OrderedNormalMCEM model = new OrderedNormalMCEM(false, iters, abseps, releps);
		NormalNoiseModel<Character> gen = new NormalNoiseModel<Character>(stuffList, 1, 1);
		
		long startTime = System.currentTimeMillis();
		
		for( int i = 0; i < trials; i++ ) {
			PreferenceProfile<Character> prefs = gen.sampleProfile(n, rnd);			
			model.setup(new NormalDistribution(0,1).sample(4));			
			ScoredItems<Character> fitted = model.fitModel(prefs).getValueMap();			
			System.out.println(fitted);	
		}		
		
		long stopTime = System.currentTimeMillis();
		
		double avgTime = (stopTime - startTime) / trials;
		System.out.printf("Fixed var: avg time for 10x4 preference profiles: %.0f ms\n", avgTime);		
	}
	
	@Test
	public void testSpeedVar() {
		int trials = 10;
		int n = 10;		
		int iters = 30;
		
		double abseps = 1e-3; // Double.NEGATIVE_INFINITY;
		double releps = 1e-3; // Double.NEGATIVE_INFINITY;					
		
		OrderedNormalMCEM model = new OrderedNormalMCEM(true, iters, abseps, releps);
		NormalNoiseModel<Character> gen = new NormalNoiseModel<Character>(stuffList, 1, 1);
		
		long startTime = System.currentTimeMillis();
		
		for( int i = 0; i < trials; i++ ) {
			PreferenceProfile<Character> prefs = gen.sampleProfile(n, rnd);			
			model.setup(new NormalDistribution(0,1).sample(4));			
			ScoredItems<Character> fitted = model.fitModel(prefs).getValueMap();			
			System.out.println(fitted);	
		}		
		
		long stopTime = System.currentTimeMillis();
		
		double avgTime = (stopTime - startTime) / trials;
		System.out.printf("Float var: avg time for 10x4 preference profiles: %.0f ms\n", avgTime);		
	}

	@Test
	public void testInference() {
		int m = 4;
		int n = 50000;		
		int iters = 50;
		double abseps = 1e-5; // Double.NEGATIVE_INFINITY;
		double releps = 1e-5; // Double.NEGATIVE_INFINITY;
		
		double[] means = TestParameterGen.randomMeanVector(m).toArray();
		double[] vars = TestParameterGen.randomVarVector(m).toArray();

		// Set to, since random sampling recenters
		means[0] = 0; vars[0] = 1;
		
		double[] sds = new double[vars.length];
		for( int i = 0; i < m; i++ ) sds[i] = Math.sqrt(vars[i]);
		
		System.out.println(Arrays.toString(means));
		System.out.println(Arrays.toString(sds));
								
		NormalNoiseModel<Character> gen = new NormalNoiseModel<Character>(stuffList, means, sds);		
		PreferenceProfile<Character> prefs = gen.sampleProfile(n, rnd);
		
		OrderedNormalMCEM model = new OrderedNormalMCEM(true, iters, abseps, releps);
		model.setup(new NormalDistribution(0,1).sample(4));		
		NormalNoiseModel<Character> fitted = model.fitModel(prefs);
		
		double[] fittedMeans = fitted.getValueMap().toArray();
		double[] fittedSds = fitted.getSigmas();
		
		System.out.println(Arrays.toString(fittedMeans));
		System.out.println(Arrays.toString(fittedSds));
		
		// This assertion must have first element of means be 0, and variances adjusted to 1
		assertArrayEquals(means, fittedMeans, 0.05);
		assertArrayEquals(sds, fittedSds, 0.05);
	}
	
}
