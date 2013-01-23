package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LogitModelsTest {

	int trials = 1000;
	int maxWins = 20;
	double tol = 1e-7;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPair() {
		/*
		 * Tests the equivalency of BT and PL on data for two alternatives.
		 */
		
		Character[] items = new Character[] { '0', '1' };				
		
		RandomGenerator rnd = new Well19937c();		
		
		for( int i = 0; i < trials; i++) {
			// Add 1 win to each side to ensure property for PLMM.
			int wins_0 = 1 + rnd.nextInt(maxWins);
			int wins_1 = 1 + rnd.nextInt(maxWins);
			
			BradleyTerryModel bt = new BradleyTerryModel();
			PlackettLuceModel pl = new PlackettLuceModel();
			
			int n = wins_0 + wins_1;
			Character[][] profile = new Character[n][2];
			
			int j = 0;
			for( ; j < wins_0; j++ ) {
				profile[j][0] = items[0];
				profile[j][1] = items[1];
			}
			
			for( ; j < n; j++ ) {
				profile[j][0] = items[1];
				profile[j][1] = items[0];				
			}							
			
			PreferenceProfile<Character> prefs = new PreferenceProfile<Character>(profile);
			
			double[] btParams = bt.fitModel(prefs).getValueMap().toArray();			
			double[] plParams = pl.fitModel(prefs).getValueMap().toArray();
			
			double btDiff = btParams[0] - btParams[1];
			double plDiff = plParams[0] - plParams[1];
			
			assertEquals(btDiff, plDiff, tol);
		}
		
	}

}
