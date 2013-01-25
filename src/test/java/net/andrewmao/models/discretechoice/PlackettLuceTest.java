package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.andrewmao.models.noise.GumbelNoiseModel;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PlackettLuceTest {

	Character[] stuff = new Character[] { 'A', 'B', 'C', 'D' };
	final List<Character> stuffList = Arrays.asList(stuff);
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSimpleOrder() {
		int n = 50;					
		Random rnd = new Random();				
		
		double strDiff = 1;
		
		GumbelNoiseModel<Character> gen = new GumbelNoiseModel<Character>(stuffList, rnd, strDiff);
		PreferenceProfile<Character> prefs = gen.sampleProfile(n);		
		
		PlackettLuceModel plmm = new PlackettLuceModel();					
		
		GumbelNoiseModel<Character> fitted = plmm.fitModel(prefs);
		ScoredItems<Character> params = fitted.getValueMap();
				
		List<Character> ranking = params.getRanking();
		
		System.out.println(params);
		System.out.println(ranking);
				
		assertEquals(stuffList, ranking);
	}

	/*
	 * Test that the Plackett-Luce model recovers the right parameter differences under the distribution
	 */
	@Test
	public void testEstimation() {
		int trials = 10;
		int size = 100000;
		double tol = 0.02;
		
		for( int a = 0; a < trials; a++ ) {
			double strDiff = Math.random();

			GumbelNoiseModel<Character> gen = new GumbelNoiseModel<Character>(stuffList, new Random(), strDiff);

			PreferenceProfile<Character> prefs = gen.sampleProfile(size);	

			PlackettLuceModel plmm = new PlackettLuceModel();
			
			GumbelNoiseModel<Character> model = plmm.fitModel(prefs);
			ScoredItems<Character> params = model.getValueMap();
			
			System.out.println(params);

			for( int i = 0; i < stuff.length; i++ ) {
				assertEquals(-i*strDiff, params.get(stuff[i]).doubleValue(), tol);
			}
		}		
	}
}
