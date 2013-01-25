package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Collection;

import net.andrewmao.models.noise.TestParameterGen;
import net.andrewmao.probability.Num;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PairwiseModelEquivalenceTest {

	static int trials = 100;
	static int maxWins = 20;
	
	static double tol_logit = 1e-7;
	static double tol_probit = 1e-3;
		
	static BradleyTerryModel bt = new BradleyTerryModel();
	static PlackettLuceModel pl = new PlackettLuceModel();
	
	static ThurstoneMostellerModel tm = new ThurstoneMostellerModel();
	static OrderedNormalEM on = new OrderedNormalEM(10, tol_probit, tol_probit);	
	
	static Character[] items = new Character[] { '0', '1' };	
	PreferenceProfile<Character> prefs;
	
	public PairwiseModelEquivalenceTest(PreferenceProfile<Character> prefs) {
		this.prefs = prefs;
	}

	@Parameters
	public static Collection<Object[]> tnImpls() {											
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

		double tmDiff = tmParams[0] - tmParams[1];
		double onDiff = onParams[0] - onParams[1];

		assertEquals(tmDiff * Num.RAC2, onDiff, tol_probit);		
	}

}
