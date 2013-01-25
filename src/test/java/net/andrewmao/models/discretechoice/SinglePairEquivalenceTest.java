package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Collection;

import net.andrewmao.models.noise.TestParameterGen;
import net.andrewmao.probability.Num;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SinglePairEquivalenceTest {

	static int trials = 10;
	static int maxWins = 20;
	
	static double tol_logit = 1e-7;
	static double accuracy_probit = 1e-5;
	static double tol_probit = 0.03;
		
	static BradleyTerryModel bt = new BradleyTerryModel();
	static PlackettLuceModel pl = new PlackettLuceModel();
	
	static ThurstoneMostellerModel tm = new ThurstoneMostellerModel();
	static OrderedNormalEM on = new OrderedNormalEM(10, accuracy_probit, accuracy_probit);
	
	OrderedNormalMCEM mcem = new OrderedNormalMCEM(false, 50, accuracy_probit, accuracy_probit);
	
	static NormalDistribution stdNormal = new NormalDistribution();
	
	static Character[] items = new Character[] { '0', '1' };	
	PreferenceProfile<Character> prefs;
	
	public SinglePairEquivalenceTest(PreferenceProfile<Character> prefs) {
		this.prefs = prefs;
	}

	@Parameters
	public static Collection<Object[]> pairwiseData() {											
		return TestParameterGen.randomPairwiseProfiles(items, trials, maxWins);
	}
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLogitModels() {
		/*
		 * Tests the equivalency of BT and PL on data for two alternatives.
		 */
		double[] btParams = bt.fitModel(prefs).getValueMap().toArray();			
		double[] plParams = pl.fitModel(prefs).getValueMap().toArray();

		double btDiff = btParams[0] - btParams[1];
		double plDiff = plParams[0] - plParams[1];

		assertEquals(btDiff, plDiff, tol_logit);				
	}
	
	@Test
	public void testProbitModels() {		
		/*
		 * Tests the equivalency of BT and PL on data for two alternatives.
		 */
		double[] tmParams = tm.fitModel(prefs).getValueMap().toArray();			
		double[] onParams = on.fitModel(prefs).getValueMap().toArray();
		
		mcem.setup(stdNormal.sample(2));
		double[] mcemParams = mcem.fitModel(prefs).getValueMap().toArray();

		double tmDiff = (tmParams[0] - tmParams[1]) * Num.RAC2;
		double onDiff = onParams[0] - onParams[1];
		double mcemDiff = mcemParams[0] - mcemParams[1];
		
		System.out.printf("TM: %.04f, MCEM: %.04f, MVNEM: %.04f\n", tmDiff, mcemDiff, onDiff);
		
		// Check against Thurstone, making adjustment
		assertTrue("MCEM differs from Thurstone", Math.abs(mcemDiff - tmDiff)/tmDiff < tol_probit);
		assertTrue("Fixed-var EM differs from Thurstone", Math.abs(onDiff - tmDiff)/tmDiff < tol_probit);
		
		// Check two EMs against each other
		assertTrue("EM models differ", Math.abs(onDiff - mcemDiff)/mcemDiff < tol_probit);
				
	}

}
