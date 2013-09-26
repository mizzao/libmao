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
	
	static OrderedNormalEM em = new OrderedNormalEM(iters, abseps, releps);	
	static OrderedNormalMCEM mcemFixed = new OrderedNormalMCEM(false, iters, abseps, releps, starting_samples, additional_samples);
	static OrderedNormalMCEM mcemVar = new OrderedNormalMCEM(true, iters, abseps, releps, starting_samples, additional_samples);
		
	List<int[]> rankings;	
	
	double[] emParams, mcemFixedParams, mcemVarParams;
	double emLL, mcemFixedLL, mcemVarLL;

	public NormalComparisonTest(List<int[]> rankings) {		
		this.rankings = rankings;
		
		NormalDistribution sample = new NormalDistribution();
		
		emParams = em.getParameters(rankings, candidates);
		emLL = em.lastLL;
		System.out.println("EM params, LL: " + emLL);
		System.out.println(Arrays.toString(emParams));		
		
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
		assertArrayEquals(emParams, mcemFixedParams, 0.02);		
	}
	
	@Test
	public void testVarLL() {
		assertTrue(mcemVarLL > emLL);
		assertTrue(mcemVarLL > mcemFixedLL);						
	}
	
	@Test
	public void testFixedLLEqual() {
		assertEquals(emLL, mcemFixedLL, Math.abs(emLL * 10 * releps));
	}

}
