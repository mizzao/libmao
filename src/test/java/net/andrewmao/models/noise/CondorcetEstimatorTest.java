package net.andrewmao.models.noise;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CondorcetEstimatorTest {

	int trials = 10;
	
	Integer[] cs = new Integer[] {1, 2, 3, 4};
	List<Integer> numbers = Arrays.asList(cs);
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEstimator() {
		int size = 10000;
		double tol = 0.02;
		
		for( int i = 0; i < trials; i++ ) {
			double phi = Math.random();
			double p = 1/(1+phi);		
			
			System.out.println("Testing p = " + p);
			CondorcetModel<Integer> generator = new CondorcetModel<Integer>(numbers, new Random(), p);		
			
			PreferenceProfile<Integer> prefs = generator.sampleProfile(size);
			
			CondorcetEstimator estimator = new CondorcetEstimator();
			
			CondorcetModel<Integer> estimated = estimator.fitModel(prefs);
			
			System.out.println("Fitted: " + estimated);
			
			assertEquals(numbers, estimated.candidates);
			assertEquals(phi, estimated.phi, tol);			
		}		
	}

}
