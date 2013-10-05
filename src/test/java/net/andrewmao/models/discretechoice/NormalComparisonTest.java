package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.andrewmao.models.noise.MeanVarParams;
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
	
	static final double MEAN_TOL = 0.02;
	
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
	
	final MeanVarParams emFixedParams, emVarParams, mcemFixedParams, mcemVarParams;
	final double emFixedLL, emVarLL, mcemFixedLL, mcemVarLL;

	public NormalComparisonTest(List<int[]> rankings) {		
		this.rankings = rankings;
		
		NormalDistribution sample = new NormalDistribution();
		double[] start = sample.sample(candidates);
		
		emFixedParams = emFixed.getParameters(rankings, candidates);
		emFixedLL = emFixedParams.fittedLikelihood;
		System.out.println("EM Fixed params, LL: " + emFixedLL);
		System.out.println(Arrays.toString(emFixedParams.mean));		
				
//		emVarParams = emVar.getParameters(rankings, candidates);
//		emVarLL = emVar.lastLL;
		emVarParams = null;
		emVarLL = 0;
//		System.out.println("EM Var params, LL: " + emVarLL);
//		System.out.println(Arrays.toString(emVarParams.mean));
		
		mcemFixed.setup(start);
		mcemFixedParams = mcemFixed.getParameters(rankings, candidates);
		mcemFixedLL = mcemFixedParams.fittedLikelihood;
		System.out.println("MCEM Fixed params, LL: " + mcemFixedLL );
		System.out.println(Arrays.toString(mcemFixedParams.mean));
		
		mcemVar.setup(start);
		mcemVarParams = mcemVar.getParameters(rankings, candidates);
		mcemVarLL = mcemVarParams.fittedLikelihood;
		System.out.println("MCEM Var params, LL: " + mcemVarLL );
		System.out.println(Arrays.toString(mcemVarParams.mean));
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
		assertArrayEquals(emFixedParams.mean, mcemFixedParams.mean, MEAN_TOL);		
	}
	
	@Test
	public void testVaryingMeanParams() {
		fail("EM Var params not implemented correctly");
		assertArrayEquals(emVarParams.mean, mcemVarParams.mean, MEAN_TOL);
	}
	
	@Test
	public void testVaryingVarParams() {
		fail("EM Var params not implemented correctly");
		assertArrayEquals(emVarParams.variance, mcemVarParams.variance, 0.05);
	}
		
	@Test
	public void testEMVarLL() {
		fail("EM Var params not implemented correctly");
		assertTrue(emVarLL > emFixedLL);								
	}
	
	@Test
	public void testMCEMVarLL() {
		assertTrue(mcemVarLL > mcemFixedLL);
	}
	
	@Test
	public void testFixedLLEqual() {
		assertEquals(emFixedLL, mcemFixedLL, Math.abs(emFixedLL * 10 * releps));
	}
	
	@Test
	public void testVarLLEqual() {
		fail("EM Var params not implemented correctly");
		assertEquals(emVarLL, mcemVarLL, Math.abs(emVarLL * 10 * releps));
	}

}
