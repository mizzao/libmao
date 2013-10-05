package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Collection;

import net.andrewmao.models.noise.GumbelNoiseModel;
import net.andrewmao.models.noise.NormalNoiseModel;
import net.andrewmao.models.noise.TestParameterGen;
import net.andrewmao.probability.NormalDist;
import net.andrewmao.probability.Num;
import net.andrewmao.socialchoice.rules.DefaultWinnerErrorMetric;
import net.andrewmao.socialchoice.rules.PreferenceProfile;
import net.andrewmao.socialchoice.rules.RankingMetric;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SinglePairEquivalenceTest {

	static int trials = 5;
	static int maxWins = 20;
	
	static double tol_logit = 1e-7;
	
	static double accuracy_probit = 1e-7;	
	static int max_em_iters = 50;
	
	static int starting_samples = 10000;	
	static int additional_samples = 5000;
		
	static double tol_probit = 0.02; // Maximum % difference tolerated
		
	static BradleyTerryModel bt = new BradleyTerryModel();
	static PlackettLuceModel pl = new PlackettLuceModel(true);
	
	static ThurstoneMostellerModel tm = new ThurstoneMostellerModel();
	static OrderedNormalEM on = new OrderedNormalEM(false, max_em_iters, accuracy_probit, accuracy_probit);
	
	OrderedNormalMCEM mcem = new OrderedNormalMCEM(false, max_em_iters, 
			accuracy_probit, accuracy_probit, starting_samples, additional_samples);
	
	static NormalDistribution stdNormal = new NormalDistribution();
	
	static Character[] items = new Character[] { '0', '1' };	
	static RankingMetric<Character> errorMetric = new DefaultWinnerErrorMetric<>();
	
	final PreferenceProfile<Character> prefs;	
	final GumbelNoiseModel<Character> btFitted;
	final GumbelNoiseModel<Character> plFitted;
	final NormalNoiseModel<Character> tmFitted;
	final NormalNoiseModel<Character> emFitted;
	final NormalNoiseModel<Character> mcemFitted;
	
	@SuppressWarnings("unchecked")
	public SinglePairEquivalenceTest(PreferenceProfile<Character> prefs) {
		this.prefs = prefs;
		
		btFitted = (GumbelNoiseModel<Character>) bt.fitModelOrdinal(prefs);
		plFitted = pl.fitModelOrdinal(prefs);
		
		tmFitted = (NormalNoiseModel<Character>) tm.fitModelOrdinal(prefs);
		System.out.println("TM: " + tmFitted.toParamString());
		
		emFitted = on.fitModelOrdinal(prefs);
		System.out.println("MVN: " + emFitted.toParamString());
		
		mcem.setup(new double[items.length]);
		mcemFitted = mcem.fitModelOrdinal(prefs);	
		System.out.println("MCEM: " + mcemFitted.toParamString());
		
		System.out.println();
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
		double[] btParams = btFitted.getValueMap().toArray();			
		double[] plParams = plFitted.getValueMap().toArray();

		double btDiff = btParams[0] - btParams[1];
		double plDiff = plParams[0] - plParams[1];

		assertEquals(btDiff, plDiff, tol_logit);				
	}
	
	@Test
	public void testLogitMetricExpectation() {
		double[] plParams = plFitted.getValueMap().toArray();
		double plError = 1 - 1d / (2 + Math.expm1(plParams[1] - plParams[0]));
		
		assertEquals(plError, plFitted.computeExpectedMetric(errorMetric), tol_logit);
	}
	
	@Test
	public void testProbitModels() {		
		/*
		 * Tests the equivalency of BT and PL on data for two alternatives.
		 */ 
		double[] tmParams = tmFitted.getValueMap().toArray();		
		double[] emParams = emFitted.getValueMap().toArray();		
		double[] mcemParams = mcemFitted.getValueMap().toArray();				
		
		double tmDiff = (tmParams[0] - tmParams[1]) * Num.RAC2; // Uses a variance of sqrt(1/2) so we need to adjust
		double onDiff = emParams[0] - emParams[1];
		double mcemDiff = mcemParams[0] - mcemParams[1];
		
		System.out.printf("TM: %.04f, MCEM: %.04f, Integral-EM: %.04f\n", tmDiff, mcemDiff, onDiff);
		
		// Check against Thurstone, making adjustment		
		assertEquals("Fixed-var EM differs from Thurstone", 0, Math.abs(onDiff/tmDiff-1), tol_probit);
		assertEquals("MCEM differs from Thurstone", 0, Math.abs(mcemDiff/tmDiff-1), tol_probit);		
		
		// Check two EMs against each other
		assertEquals("EM models differ", 0, Math.abs(onDiff/mcemDiff-1), tol_probit);
				
	}

	@Test
	public void testProbitMetricExpectation() {
		double[] normalParams = tmFitted.getValueMap().toArray();
		double normalError = 1 - NormalDist.cdf01(normalParams[0] - normalParams[1]);
		
		assertEquals(normalError, tmFitted.computeExpectedMetric(errorMetric), accuracy_probit);
	}
}
