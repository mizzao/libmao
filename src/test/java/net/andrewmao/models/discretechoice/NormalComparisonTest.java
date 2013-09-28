package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.andrewmao.models.noise.TestParameterGen;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class NormalComparisonTest {
	
	static int candidates = 4;
	static int trials = 5;
	static int voters = 10;
		
	static double abseps = 1e-5;
	static double releps = 1e-5;
	
	static int iters = 50;	
	static int starting_samples = 3000;	
	static int additional_samples = 500;		
	
	static OrderedNormalEM emFixed = new OrderedNormalEM(false, iters, abseps, releps);
	static OrderedNormalEM emVar = new OrderedNormalEM(true, iters, abseps, releps);	
	static OrderedNormalMCEM mcemFixed = new OrderedNormalMCEM(false, iters, abseps, releps, 
			starting_samples, additional_samples);
	static OrderedNormalMCEM mcemVar = new OrderedNormalMCEM(true, iters, abseps, releps, 
			starting_samples, additional_samples);
		
	List<int[]> rankings;	
	
	double[] emFixedParams, emVarParams, mcemFixedParams, mcemVarParams;
	double emFixedLL, emVarLL, mcemFixedLL, mcemVarLL;

	public NormalComparisonTest(List<int[]> rankings) {		
		this.rankings = rankings;
		
		NormalDistribution sample = new NormalDistribution();
		
		emFixedParams = emFixed.getParameters(rankings, candidates);
		emFixedLL = emFixed.lastLL;
		System.out.println("EM Fixed params, LL: " + emFixedLL);
		System.out.println(Arrays.toString(emFixedParams));		
		
		emVarParams = emVar.getParameters(rankings, candidates);
		emVarLL = emVar.lastLL;
		System.out.println("EM Var params, LL: " + emVarLL);
		System.out.println(Arrays.toString(emVarParams));
		
		mcemFixed.setup(new double[candidates]);
		mcemFixedParams = mcemFixed.getParameters(rankings, candidates);
		mcemFixedLL = mcemFixed.lastLL;
		System.out.println("MCEM Fixed params, LL: " + mcemFixedLL );
		System.out.println(Arrays.toString(mcemFixedParams));
		
		mcemVar.setup(sample.sample(candidates));
		mcemVarParams = mcemVar.getParameters(rankings, candidates);
		mcemVarLL = mcemVar.lastLL;
		System.out.println("MCEM Var params, LL: " + mcemVarLL );
		System.out.println(Arrays.toString(mcemVarParams));
	}
	
	@Parameters
	public static Collection<Object[]> tnImpls() {											
		return TestParameterGen.randomProfiles(candidates, trials, voters);
	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFixedParams() {
		assertArrayEquals(emFixedParams, mcemFixedParams, 0.02);		
	}
	
	@Test
	public void testVarParams() {
		assertArrayEquals(emVarParams, mcemVarParams, 0.02);
	}
	
	@Test
	public void testVarLL() {
		assertTrue(mcemVarLL > emFixedLL);
		assertTrue(mcemVarLL > mcemFixedLL);						
	}
	
	@Test
	public void testFixedLLEqual() {
		assertEquals(emFixedLL, mcemFixedLL, Math.abs(emFixedLL * 10 * releps));
	}
	
	@Test
	public void testVarLLEqual() {
		assertEquals(emVarLL, mcemVarLL, Math.abs(emVarLL * 10 * releps));
	}

}
