package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import net.andrewmao.models.noise.TestParameterGen;

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
	static int gibbsSamples = 10000;
	
	static double tol = 1e-2;
	
	RealVector mean, var;
	int[] ranking;

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
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConditionalExpectation() {		
		double[] condExpMVN = OrderedNormalEM.conditionalExp(mean, var, ranking);
		
		double[] condExpGibbs = new NormalGibbsSampler(mean, var, ranking, gibbsSamples).call().m1;
		
		System.out.println("MVN: " + Arrays.toString(condExpMVN));
		System.out.println("Gibbs: " + Arrays.toString(condExpGibbs));
		
		assertArrayEquals(condExpMVN, condExpGibbs, tol);
	}

}
