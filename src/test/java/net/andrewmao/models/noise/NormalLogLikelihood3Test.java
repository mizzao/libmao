package net.andrewmao.models.noise;

import static org.junit.Assert.*;

import java.util.Collection;

import org.apache.commons.math3.linear.RealVector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class NormalLogLikelihood3Test {
	
	static int trials = 1000;	
	
	RealVector mean, var;
	int[] ranking;

	public NormalLogLikelihood3Test(RealVector mean, RealVector var, int[] ranking ) {
		this.mean = mean;
		this.var = var;
		this.ranking = ranking;
	}

	@Parameters
	public static Collection<Object[]> tnImpls() {											
		return TestParameterGen.randomMeanVarRankings(3, trials);
	}
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void test3xLikelihood() {		
		double tol = 1e-5;		
					
		NormalLogLikelihood ll = new NormalLogLikelihood(mean, var);
						
		double bivariateLL = ll.bivariateLL(ranking);
		double multivariateLL = ll.multivariateLL(ranking);

		assertEquals(bivariateLL, multivariateLL, tol);				
	}

}
