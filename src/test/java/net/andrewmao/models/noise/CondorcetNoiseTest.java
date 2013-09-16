package net.andrewmao.models.noise;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import net.andrewmao.models.noise.CondorcetModel;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class CondorcetNoiseTest {

	CondorcetModel<Integer> gen;	
	Integer[] cs = new Integer[] {1, 2, 3, 4};
	List<Integer> numbers = Arrays.asList(cs);
	
	Character[] ls = new Character[] { 'a', 'b', 'c', 'd' };
	List<Character> letters = Arrays.asList(ls);
	
	Comparator<Integer> comp = new Comparator<Integer> () {
		@Override
		public int compare(Integer o1, Integer o2) {
			return o1.compareTo(o2);
		}			
	};
	
	Random r = new Random();
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testParamRecovery() {		
		Collection<List<Integer>> mixed = Arrays.asList(
				Arrays.asList(1, 2, 3, 4),
				Arrays.asList(2, 1, 4, 3)
				);
		double prob = 0.6;
		
		CondorcetModel<Integer> model = new CondorcetModel<Integer>(mixed, prob);
		
		String params = model.toParamString();
		System.out.println(params);
		
		// TODO parse model from string
	}
	
	@Test
	public void testWeights() {
		System.out.println("Testing weights");
		double p = 0.6;		
		double phi = (1-p)/p;
		
		for( int i = 0; i < ls.length; i++ ) {
			double[] wts = CondorcetModel.getWeights(i, phi);
			
			double[] test = new double[wts.length];
			for( int j = 0; j < test.length; j++ )
				test[j] = Math.pow(phi, i - j);
			
			System.out.println(Arrays.toString(test));
			assertTrue(Arrays.equals(wts, test));			
		}
	}

	/*
	 * Tests the insertion vector on page 3 of lu/boutilier
	 */
	@Test	
	public void testInsertion() {
		System.out.println("Testing insertion vector");
		
		CondorcetModel<Character> charGen = new CondorcetModel<Character>(letters, 1);
		
		int[] insvec = new int[] {0, 0, 1, 2};
		Character[] expected = new Character[] {'b', 'c', 'd', 'a'};
		
		List<Character> inserted = charGen.getInsertedList(insvec);
		
		assertArrayEquals(inserted.toArray(), expected);
	}
	
	@Test
	public void testUniform() {
		System.out.println("Testing uniform distribution...p = 1/2");
		int size = 10000;
		double tol = 0.02;
		double p = 0.5;			
		
		gen = new CondorcetModel<Integer>(numbers, p);		
		
		PreferenceProfile<Integer> prefs = gen.sampleProfile(size, r);
		
		for( int i = 0; i < cs.length; i++ ) {
			for( int j = i+1; j < cs.length; j++ ) {
				int corr = prefs.getNumCorrect(cs[i], cs[j], comp);
				double pct = 1.0 * corr / size;
				System.out.printf("%d, %d: %d/%d (%.04f)\n", cs[i], cs[j], corr, size, pct);
				
				assertTrue(Math.abs(pct - p) < tol);
			}
		}
	}
	
	@Test
	public void testConstant() {
		System.out.println("Testing constant distribution...p = 1");
		int size = 10000;					
		
		gen = new CondorcetModel<Integer>(numbers, 1);		
		
		PreferenceProfile<Integer> prefs = gen.sampleProfile(size, r);
		
		for( int i = 0; i < cs.length; i++ ) {
			for( int j = i+1; j < cs.length; j++ ) {
				int corr = prefs.getNumCorrect(cs[i], cs[j], comp);
				double pct = 1.0 * corr / size;
				System.out.printf("%d, %d: %d/%d (%.04f)\n", cs[i], cs[j], corr, size, pct);
				
				assertTrue(corr == size);
			}
		}
	}

	@Test
	public void testGeneration() {		
		int size = 10000;
		double tol = 0.02;
		double p = 0.8;
		double phi = (1-p)/p;
		
		System.out.println("Testing p = " + p);
		gen = new CondorcetModel<Integer>(numbers, p);		
		
		PreferenceProfile<Integer> prefs = gen.sampleProfile(size, r);
		
		for( int i = 0; i < cs.length; i++ ) {
			for( int j = i+1; j < cs.length; j++ ) {
				int corr = prefs.getNumCorrect(cs[i], cs[j], comp);
				double pct = 1.0 * corr / size;
				System.out.printf("%d, %d: %d/%d (%.04f) ", cs[i], cs[j], corr, size, pct);
				
				if( j - i == 1) {
					assertTrue(Math.abs(pct - p) < tol);
					System.out.println("Model: " + p);
				}
				else if(j - i == 2) {
					double prob = (1 + 2*phi)/(1+phi)/(1 + phi + phi*phi);
					assertTrue(Math.abs(pct - prob) < tol);
					System.out.println("Model: " + prob);
				}
				else if(j - i == 3) {
					double prob = (1+2*phi+3*phi*phi)/(1+phi+phi*phi)/(1+phi+phi*phi+phi*phi*phi);
					assertTrue(Math.abs(pct - prob) < tol);
					System.out.println("Model: " + prob);
				}
			}
		}				
	}

}
