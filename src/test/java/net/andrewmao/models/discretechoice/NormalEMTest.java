package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.andrewmao.models.noise.NormalNoiseModel;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NormalEMTest {
	
	Character[] stuff = new Character[] { 'A', 'B', 'C', 'D' };
	final List<Character> stuffList = Arrays.asList(stuff);
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInference() {
		int trials = 10;
		
		int size = 1000;
		double tol = 0.02;
		
		// TODO: this is still busted 
				
		for( int a = 0; a < trials; a++ ) {
			double strDiff = Math.random();
			System.out.println("Testing " + strDiff);

			NormalNoiseModel<Character> gen = new NormalNoiseModel<Character>(stuffList, new Random(), strDiff, 1.0d);

			PreferenceProfile<Character> prefs = gen.sampleProfile(size);	

			OrderedNormalEM normalEM = new OrderedNormalEM(30, 1e-6, 1e-3);
			
			NormalNoiseModel<Character> model = normalEM.fitModel(prefs);
			ScoredItems<Character> params = model.getValueMap();
			
			System.out.println(params);

			for( int i = 0; i < stuff.length; i++ ) {
				assertEquals(-i*strDiff, params.get(stuff[i]).doubleValue(), tol);
			}
		}	
	}

}
