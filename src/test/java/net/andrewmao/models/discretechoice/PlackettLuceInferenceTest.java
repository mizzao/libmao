package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.andrewmao.models.noise.GumbelNoiseModel;
import net.andrewmao.models.noise.TestParameterGen;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.apache.commons.math3.linear.RealVector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PlackettLuceInferenceTest {

	static int trials = 10;
	static int size = 100000;
	
	static Character[] stuff = new Character[] { 'A', 'B', 'C', 'D' };
	static final List<Character> stuffList = Arrays.asList(stuff);
	
	Random rnd = new Random();
			
	RealVector mean;
	
	public PlackettLuceInferenceTest(RealVector mean) {
		this.mean = mean;		
	}
	
	@Parameters
	public static Collection<Object[]> randomVals() {											
		return TestParameterGen.randomMeanVectors(stuff.length, trials);
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/*
	 * Test that the Plackett-Luce model recovers the right parameter differences under the Gumbel distribution
	 */
	@Test
	public void testEstimation() {		
		double tol = 0.02;
					
		GumbelNoiseModel<Character> gen = new GumbelNoiseModel<Character>(stuffList, rnd, mean.toArray());
		PreferenceProfile<Character> prefs = gen.sampleProfile(size);
		double dataLL = gen.logLikelihood(prefs);

		PlackettLuceModel plmm = new PlackettLuceModel();			
		GumbelNoiseModel<Character> model = plmm.fitModel(prefs);
		double fittedLL = plmm.lastComputedLL;
		double modelLL = model.logLikelihood(prefs);

		ScoredItems<Character> params = model.getValueMap();			
		System.out.println(params);

		System.out.println("Last fitted LL:" + fittedLL);
		System.out.println("Final Model LL: " + modelLL);
		System.out.println("Original Data LL: " + dataLL);

		assertTrue("Final model LL doesn't match fitted LL", 
				Math.abs(fittedLL - modelLL) / modelLL < PlackettLuceModel.tolerance );
		assertTrue("Original data LL too far from fitted model LL",
				Math.abs(fittedLL - modelLL) / modelLL < 1e-6 );

		for( int i = 0; i < stuff.length; i++ ) {
			assertEquals(mean.getEntry(i) - mean.getEntry(0),
					params.get(stuff[i]).doubleValue(), tol);
		}
				
	}
}
