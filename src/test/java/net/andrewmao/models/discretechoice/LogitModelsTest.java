package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

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
		List<Character> itemsList = Arrays.asList(items);
		Character[] items_rev = new Character[] {'1', '0'};
		
		RandomGenerator rnd = new Well19937c();		
		
		for( int i = 0; i < trials; i++) {
			// Add 1 win to each side to ensure property for PLMM.
			int wins_0 = 1 + rnd.nextInt(maxWins);
			int wins_1 = 1 + rnd.nextInt(maxWins);
			
			BradleyTerryModel<Character> bt = new BradleyTerryModel<Character>(itemsList);
			PlackettLuceModel<Character> pl = new PlackettLuceModel<Character>(itemsList);
			
			bt.addData('0', '1', wins_0);
			bt.addData('1', '0', wins_1);
			
			for( int j = 0; j < wins_0; j++ ) pl.addData(items);
			for( int j = 0; j < wins_1; j++ ) pl.addData(items_rev);
			
			double[] btParams = bt.getParameters().toArray();			
			double[] plParams = pl.getParameters().toArray();
			
			double btDiff = btParams[0] - btParams[1];
			double plDiff = plParams[0] - plParams[1];
			
			assertEquals(btDiff, plDiff, tol);
		}
		
	}

}
