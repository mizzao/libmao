package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.andrewmao.models.noise.NormalNoiseModel;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.junit.*;

public class NormalEMTest {
	
	static final double llTol = 1e-4;
	
	static Random rnd = new Random();
	
	Character[] stuff = new Character[] { 'A', 'B', 'C', 'D' };
	final List<Character> stuffList = Arrays.asList(stuff);
	
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testNanBug() {
		int iters = 30;
		
		double abseps = 1e-3; // Double.NEGATIVE_INFINITY;
		double releps = 1e-3; // Double.NEGATIVE_INFINITY;		
		
		PreferenceProfile<Integer> prefs = 
				new PreferenceProfile<Integer>(new Integer[][] {
						{8, 5, 11, 14},
						{8, 14, 11, 5},
						{8, 5, 14, 11},
						{5, 8, 11, 14},
						{8, 5, 14, 11},
						{5, 8, 11, 14},
						{14, 5, 11, 8},
						{5, 8, 14, 11},
						{14, 8, 5, 11},
						{8, 5, 11, 14},
				});
		
		OrderedNormalEM model = new OrderedNormalEM(iters, abseps, releps);
		NormalNoiseModel<Integer> fitted = model.fitModelOrdinal(prefs);
		
		System.out.println(fitted.toParamString());
		
		for( double d : fitted.getValueMap().toArray() )
			assertFalse(Double.isNaN(d));	
	}
	
	@Test
	public void testSpeed() {
		int trials = 10;
		int n = 10;		
		int iters = 30;
		
		double abseps = 1e-3; // Double.NEGATIVE_INFINITY;
		double releps = 1e-3; // Double.NEGATIVE_INFINITY;					
		
		OrderedNormalEM model = new OrderedNormalEM(iters, abseps, releps);
		NormalNoiseModel<Character> gen = new NormalNoiseModel<Character>(stuffList, 1, 1);
		
		long startTime = System.currentTimeMillis();
		
		for( int i = 0; i < trials; i++ ) {
			PreferenceProfile<Character> prefs = gen.sampleProfile(n, rnd);						
			ScoredItems<Character> fitted = model.fitModelOrdinal(prefs).getValueMap();			
			System.out.println(fitted);	
		}
		
		long stopTime = System.currentTimeMillis();
		
		double avgTime = (stopTime - startTime) / trials;
		System.out.printf("Avg time for 10x4 preference profiles: %.0f ms\n", avgTime);		
	}
	
	@Test
	public void testInference() {		
		int size = 10000;
		double tol = 0.02;		
		 					
		double strDiff = 1.0;
		System.out.println("Testing " + strDiff);

		NormalNoiseModel<Character> gen = new NormalNoiseModel<Character>(stuffList, strDiff, 1.0d);			

		PreferenceProfile<Character> prefs = gen.sampleProfile(size, rnd);	
		double targetLL = gen.logLikelihood(prefs);
		System.out.println("Target likelihood: " + targetLL);

		OrderedNormalEM normalEM = new OrderedNormalEM(100, 1e-5, 1e-5);

		NormalNoiseModel<Character> model = normalEM.fitModelOrdinal(prefs);
		double achievedLL = model.logLikelihood(prefs);
		System.out.println("Achieved likelihood: " + achievedLL);		
		
		ScoredItems<Character> params = model.getValueMap();

		System.out.println(params);

		assertTrue("LL didn't reach close to target", Math.abs(achievedLL/targetLL-1) < llTol);

		for( int i = 0; i < stuff.length; i++ ) {
			assertEquals(-i*strDiff, params.get(stuff[i]).doubleValue(), tol);
		}
			
	}

}
