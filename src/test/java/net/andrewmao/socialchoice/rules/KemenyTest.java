package net.andrewmao.socialchoice.rules;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.andrewmao.models.discretechoice.ThurstoneMostellerModel;
import net.andrewmao.models.noise.CondorcetEstimator;
import net.andrewmao.models.noise.CondorcetModel;
import net.andrewmao.models.noise.NoiseModel;
import net.andrewmao.models.noise.NormalNoiseModel;
import net.andrewmao.socialchoice.rules.Kemeny;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class KemenyTest {

	Kemeny kemeny;
	Kemeny_Old kemeny_old;
	Kemeny_Slow kemeny_slow;
	
	@Before
	public void setUp() throws Exception {
		kemeny = new Kemeny();
		kemeny_old = new Kemeny_Old();
		kemeny_slow = new Kemeny_Slow();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRankingsThree() {
		
		PreferenceProfile<Integer> prefs = new PreferenceProfile<Integer>(new Integer[][] {
			{3, 2, 1, 4}, 
			{2, 1, 3, 4},	
				});
		
		List<List<Integer>> rankings = kemeny.getAllRankings(prefs);		
		
		assertEquals(rankings.size(), 3);
		assertTrue(rankings.contains(Arrays.asList(new Integer[] {3, 2, 1, 4})));
		assertTrue(rankings.contains(Arrays.asList(new Integer[] {2, 3, 1, 4})));
		assertTrue(rankings.contains(Arrays.asList(new Integer[] {2, 1, 3, 4})));
	}
	
	@Test
	public void testRankingsTwo() {
		
		PreferenceProfile<Integer> prefs = new PreferenceProfile<Integer>(new Integer[][] {
			{1, 2, 3, 4}, 
			{2, 1, 3, 4},	
				});
		
		List<List<Integer>> rankings = kemeny.getAllRankings(prefs);
		
		assertEquals(rankings.size(), 2);
		assertTrue(rankings.contains(Arrays.asList(new Integer[] {1, 2, 3, 4})));
		assertTrue(rankings.contains(Arrays.asList(new Integer[] {2, 1, 3, 4})));
	}
	
	@Test
	public void testRankingsBackward() {
		
		PreferenceProfile<Integer> prefs = new PreferenceProfile<Integer>(new Integer[][] {
			{4, 3, 2, 1}, 
			{4, 3, 2, 1},	
				});
		
		List<List<Integer>> rankings = kemeny.getAllRankings(prefs);
		
		assertEquals(rankings.size(), 1);
		assertTrue(rankings.contains(Arrays.asList(new Integer[] {4, 3, 2, 1})));		
	}
	
	@Test
	public void testRankingsMixed() {
		
		PreferenceProfile<Integer> prefs = new PreferenceProfile<Integer>(new Integer[][] {
			{2, 1, 4, 3}, 
			{2, 1, 4, 3},	
				});
		
		List<List<Integer>> rankings = kemeny.getAllRankings(prefs);
		
		assertEquals(rankings.size(), 1);
		assertTrue(rankings.contains(Arrays.asList(new Integer[] {2, 1, 4, 3})));		
	}
	
	@Test 
	public void testFuckyRankings() {
		// Infamous kemeny bug that John helped me with, here is a test case for it
		PreferenceProfile<Integer> prefs = new PreferenceProfile<Integer>(new Integer[][] {
				{1, 3, 2, 4},
				{2, 1, 4, 3},
				{4, 1, 3, 2},	
					});
		
		List<List<Integer>> rankings = kemeny.getAllRankings(prefs);

		// All rankings 6 from end
		assertEquals(rankings.size(), 3);
		assertTrue(rankings.contains(Arrays.asList(new Integer[] {1, 2, 4, 3})));
		assertTrue(rankings.contains(Arrays.asList(new Integer[] {1, 3, 2, 4})));
		assertTrue(rankings.contains(Arrays.asList(new Integer[] {1, 4, 3, 2})));				
	}
	
	@Test 
	public void testTiedRankingsCondorcet() {		
		// All tied rankings should have the same p...?
		
		PreferenceProfile<Integer> prefs = new PreferenceProfile<Integer>(new Integer[][] {
				{1, 3, 2, 4},
				{2, 1, 4, 3},
				{4, 1, 3, 2},	
					});
		
		CondorcetEstimator estimator = new CondorcetEstimator();		
		CondorcetModel<Integer> estimated = estimator.fitModelOrdinal(prefs);
		
		System.out.println(estimated);
	}
	
	@Test
	public void testKemenyDifferent() {
		// Make sure all Kemenys are doing the same thing :D
		int trials = 1000;
		
		List<Integer> stuff = Arrays.asList(new Integer[] {1, 2, 3, 4});		
		NoiseModel<Integer> rfg = new NormalNoiseModel<Integer>(stuff, 0.2, ThurstoneMostellerModel.THURSTONE_SIGMA);
		
		for( int i = 0; i < trials; i++ ) {
			PreferenceProfile<Integer> generated = rfg.sampleProfile(10, new Random());
			
			List<List<Integer>> k1 = kemeny.getAllRankings(generated);
			List<List<Integer>> k2 = kemeny_old.getAllRankings(generated);
			List<List<Integer>> k3 = kemeny_slow.getAllRankings(generated);
			
			if( !(k1.containsAll(k2) && k2.containsAll(k3) && k3.containsAll(k1))) {
				System.out.println(generated);
				
				Integer[] arr = new Integer[generated.getNumCandidates()];
				System.out.println("Kemeny: " + k1.size());
				for( List<Integer> ranking : k1 ) {					
					System.out.println(ranking.toString() + Kemeny_Old.kendallTau(generated.profile, ranking.toArray(arr)));					
				}
				System.out.println("Kemeny_Old: " + k2.size());
				for( List<Integer> ranking : k2 ) {					
					System.out.println(ranking.toString() + Kemeny_Old.kendallTau(generated.profile, ranking.toArray(arr)));					
				}
				System.out.println("Kemeny_Slow: " + k3.size());
				for( List<Integer> ranking : k3 ) {					
					System.out.println(ranking.toString() + Kemeny_Old.kendallTau(generated.profile, ranking.toArray(arr)));					
				}
				fail();
			}
		}		
	}
	
	@Ignore
	public void testRankingsTen() {
		
		PreferenceProfile<Integer> prefs = new PreferenceProfile<Integer>(new Integer[][] {
			{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 
			{2, 1, 3, 4, 5, 6, 7, 8, 9, 10},	
				});
		
		List<List<Integer>> rankings = kemeny.getAllRankings(prefs);
		
		assertEquals(rankings.size(), 2);
		assertTrue(rankings.contains(Arrays.asList(new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})));
		assertTrue(rankings.contains(Arrays.asList(new Integer[] {2, 1, 3, 4, 5, 6, 7, 8, 9, 10})));
	}

	@Ignore
	public void testRankingsTenTwo() {
		
		PreferenceProfile<Integer> prefs = new PreferenceProfile<Integer>(new Integer[][] {
			{3, 2, 1, 4, 5, 6, 7, 8, 9, 10}, 
			{2, 1, 3, 4, 5, 6, 7, 8, 9, 10},	
				});
		
		List<List<Integer>> rankings = kemeny.getAllRankings(prefs);		
		
		assertEquals(rankings.size(), 3);
		assertTrue(rankings.contains(Arrays.asList(new Integer[] {3, 2, 1, 4, 5, 6, 7, 8, 9, 10})));
		assertTrue(rankings.contains(Arrays.asList(new Integer[] {2, 3, 1, 4, 5, 6, 7, 8, 9, 10})));
		assertTrue(rankings.contains(Arrays.asList(new Integer[] {2, 1, 3, 4, 5, 6, 7, 8, 9, 10})));
	}
}
