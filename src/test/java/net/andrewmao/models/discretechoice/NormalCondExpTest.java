package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import net.andrewmao.models.noise.NormalLogLikelihood;
import net.andrewmao.models.noise.TestParameterGen;
import net.andrewmao.probability.MultivariateNormal.CDFResult;
import net.andrewmao.probability.MultivariateNormal.ExpResult;

import org.apache.commons.math3.linear.RealVector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests conditional expectation - MVN exp and gibbs sampling
 * @author mao
 *
 */
@RunWith(Parameterized.class)
public class NormalCondExpTest {

	static int trials = 10;	
	static int gibbsSamplesHigh = 100000;
	
	static double tol = 0.02;
	
	RealVector mean, var;
	int[] ranking;

	double[] condExpMVN;
	
	public NormalCondExpTest(RealVector mean, RealVector var, int[] ranking ) {
		this.mean = mean;
		this.var = var;
		this.ranking = ranking;
	}

	@Parameters
	public static Collection<Object[]> tnImpls() {											
		return TestParameterGen.randomMeanVarRankings(4, trials);
	}
	
	@Before
	public void setUp() throws Exception {
		condExpMVN = OrderedNormalEM.conditionalExp(mean, var, ranking, 4, 1e-8);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testLLEquivalence() {				
		// Ensure that we can use the conditional expectation MVN value to estimate likelihood		
		
		CDFResult cdf = NormalLogLikelihood.multivariateProb(mean, var, ranking);
		ExpResult exp = OrderedNormalEM.multivariateExp(mean, var, ranking, 1, null);
		
		assertEquals(cdf.value, exp.cdf, 1e-4);
	}
	
	@Test
	public void testConditionalExpectationQuick() {
		System.out.println("Testing bias ");
		double[] condExpMVNQuick = OrderedNormalEM.conditionalExp(mean, var, ranking, 1, 1e-5);		
						
		System.out.println("MVN Quick: " + Arrays.toString(condExpMVNQuick));		
		System.out.println("MVN Accurate: " + Arrays.toString(condExpMVN));
		
		// check rankings are consistent
		assertTrue("MVN Quick conditional expectation is inconsistent", checkConsistency(condExpMVNQuick));
		assertTrue("MVN conditional expectation is inconsistent", checkConsistency(condExpMVN));				
		
		assertArrayEquals(condExpMVN, condExpMVNQuick, tol);
	}

	@Test
	public void testConditionalExpectationGibbs() {
		System.out.println("Testing Gibbs ");
		double[] condExpGibbs = new NormalGibbsSampler(mean, var, ranking, gibbsSamplesHigh).call().m1;
				
		System.out.println("MVN: " + Arrays.toString(condExpMVN));		
		System.out.println("Gibbs: " + Arrays.toString(condExpGibbs));
		
		// check rankings are consistent
		assertTrue("MVN conditional expectation is inconsistent", checkConsistency(condExpMVN));
		assertTrue("Gibbs conditional expectation is inconsistent", checkConsistency(condExpGibbs));				
		
		assertArrayEquals(condExpMVN, condExpGibbs, tol);
	}

	private boolean checkConsistency(double[] condExp) {
		for( int i = 1; i < ranking.length; i++ ) {
			// It's wrong if the value(i) > value(i-1)
			if( condExp[ranking[i]-1] > condExp[ranking[i-1]-1] ) {
				return false;
			}
		}
		return true;
	}

}
