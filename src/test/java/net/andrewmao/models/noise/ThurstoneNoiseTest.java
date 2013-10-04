package net.andrewmao.models.noise;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import net.andrewmao.models.discretechoice.ThurstoneMostellerModel;
import net.andrewmao.models.noise.NormalNoiseModel;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ThurstoneNoiseTest {
	
	// Lots of room over the 16 decimals that the normal approximation is supposed to give
	static final double EXACT_TOL = 1e-12; 
	
	Character[] ls = new Character[] { 'a', 'b', 'c', 'd' };
	List<Character> letters = Arrays.asList(ls);
	
	Comparator<Character> comp = new Comparator<Character>() {
		@Override
		public int compare(Character o1, Character o2) {			
			return new Character(o1).compareTo(o2);
		}		
	};
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		int size = 100000;
		double tol = 0.02;
		double strDiff = 0.5;
		
		NormalDistribution dist = new NormalDistribution(0, 1);
		
		NormalNoiseModel<Character> gen = new NormalNoiseModel<Character>(letters, strDiff, ThurstoneMostellerModel.THURSTONE_SIGMA);
		System.out.println(gen.toParamString());
						
		PreferenceProfile<Character> prefs = gen.sampleProfile(size, new Random());
		
		double prob1 = dist.cumulativeProbability(strDiff);
//		System.out.println(1.0 * prefs.getNumCorrect('a', 'b', comp) / size);
//		System.out.println(prob1);
		assertEquals(prob1, gen.marginalProbability('a', 'b'), EXACT_TOL);
		assertEquals(prob1, gen.marginalProbability('b', 'c'), EXACT_TOL);
		assertEquals(prob1, gen.marginalProbability('c', 'd'), EXACT_TOL);
		assertEquals(1-prob1, gen.marginalProbability('d', 'c'), EXACT_TOL);
		assertEquals(1-prob1, gen.marginalProbability('c', 'b'), EXACT_TOL);
		assertEquals(1-prob1, gen.marginalProbability('b', 'a'), EXACT_TOL);
		
		double prob2 = dist.cumulativeProbability(2*strDiff);
//		System.out.println(1.0 * prefs.getNumCorrect('a', 'c', comp) / size);
//		System.out.println(prob2);
		assertEquals(prob2, gen.marginalProbability('a', 'c'), EXACT_TOL);
		assertEquals(prob2, gen.marginalProbability('b', 'd'), EXACT_TOL);
		assertEquals(1-prob2, gen.marginalProbability('c', 'a'), EXACT_TOL);
		assertEquals(1-prob2, gen.marginalProbability('d', 'b'), EXACT_TOL);
		
		double prob3 = dist.cumulativeProbability(3*strDiff);
//		System.out.println(1.0 * prefs.getNumCorrect('a', 'd', comp) / size);
//		System.out.println(prob3);
		assertEquals(prob3, gen.marginalProbability('a', 'd'), EXACT_TOL);
		assertEquals(1-prob3, gen.marginalProbability('d', 'a'), EXACT_TOL);		
				
		assertEquals(prob1, 1.0 * prefs.getNumCorrect('a', 'b', comp) / size, tol);
		assertEquals(prob1, 1.0 * prefs.getNumCorrect('b', 'c', comp) / size, tol);
		assertEquals(prob1, 1.0 * prefs.getNumCorrect('c', 'd', comp) / size, tol);
		
		assertEquals(prob2, 1.0 * prefs.getNumCorrect('a', 'c', comp) / size, tol);
		assertEquals(prob2, 1.0 * prefs.getNumCorrect('b', 'd', comp) / size, tol);
		
		assertEquals(prob3, 1.0 * prefs.getNumCorrect('a', 'd', comp) / size, tol);
	}

}
