package net.andrewmao.socialchoice.rules;

import static org.junit.Assert.*;

import java.util.Arrays;

import net.andrewmao.socialchoice.rules.Maximin;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MaximinTest {

	Maximin rule;
	
	@Before
	public void setUp() throws Exception {
		rule = new Maximin();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		Integer[][] profile = {
				{1, 2, 3, 4},
				{1, 2, 3, 4},
				{2, 1, 3, 4},
		};
		
		PreferenceProfile<Integer> prefs = new PreferenceProfile<Integer>(profile);				
		
		int[] mmScores = rule.getMaximinScores(prefs);
		int[] expected = new int[] { 2, 1, 0, 0 };
		
		System.out.println(Arrays.toString(mmScores));
				
		assertArrayEquals(expected, mmScores);		
		
	}

	@Test
	public void testPerfect() {
		Integer[][] profile = {
				{1, 2, 3, 4},
				{1, 2, 3, 4},
				{1, 2, 3, 4},
		};
		
		PreferenceProfile<Integer> prefs = new PreferenceProfile<Integer>(profile);				
		
		int[] mmScores = rule.getMaximinScores(prefs);
		int[] expected = new int[] { 3, 0, 0, 0 };
		
		System.out.println(Arrays.toString(mmScores));
				
		assertArrayEquals(expected, mmScores);		
		
	}
	
	@Test
	public void test2Perfect() {
		Integer[][] profile = {
				{1, 2, 3, 4},
				{2, 1, 3, 4},
				{3, 2, 1, 4},
		};
		
		PreferenceProfile<Integer> prefs = new PreferenceProfile<Integer>(profile);				
		
		int[] mmScores = rule.getMaximinScores(prefs);
		int[] expected = new int[] { 1, 2, 1, 0 };
		
		System.out.println(Arrays.toString(mmScores));
				
		assertArrayEquals(expected, mmScores);		
		
	}
}
