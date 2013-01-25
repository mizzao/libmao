package net.andrewmao.models.noise;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.linear.RealVector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class NormalLogLikelihood4Test {
	
	static int trials = 20;		
	static int n = 100;
	
	RealVector mean, var;	
	List<int[]> rankings;

	public NormalLogLikelihood4Test(RealVector mean, RealVector var, List<int[]> rankings) {
		this.mean = mean;
		this.var = var;	
		this.rankings = rankings;
	}
	
	@Parameters
	public static Collection<Object[]> tnImpls() {											
		return TestParameterGen.randomMeanVarProfiles(4, trials, n);
	}
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test4xLikelihoodOptimized() {
		double tol = 1e-3; // Differ less than 0.1%
		
		NormalLogLikelihood ll = new NormalLogLikelihood(mean, var);

		double smartLL = ll.logLikelihood(rankings);
		double dumbll = ll.logLikelihoodDumb(rankings);

		assertTrue(Math.abs((dumbll - smartLL)/ smartLL) < tol);			
		
	}

}
