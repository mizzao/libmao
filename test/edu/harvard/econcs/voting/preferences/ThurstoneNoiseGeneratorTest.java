package edu.harvard.econcs.voting.preferences;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.harvard.econcs.voting.rules.PreferenceProfile;

public class ThurstoneNoiseGeneratorTest {
	
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
		
		ThurstoneNoiseGenerator<Character> gen = new ThurstoneNoiseGenerator<Character>(letters, new Random(), strDiff);
						
		PreferenceProfile<Character> prefs = gen.getRandomProfile(size);
		
		System.out.println(1.0 * prefs.getNumCorrect('a', 'b', comp) / size);
		System.out.println(dist.cumulativeProbability(strDiff));
		
		System.out.println(1.0 * prefs.getNumCorrect('a', 'c', comp) / size);
		System.out.println(dist.cumulativeProbability(2*strDiff));
		
		System.out.println(1.0 * prefs.getNumCorrect('a', 'd', comp) / size);
		System.out.println(dist.cumulativeProbability(3*strDiff));
				
		assertEquals(1.0 * prefs.getNumCorrect('a', 'b', comp) / size, dist.cumulativeProbability(strDiff), tol);
		assertEquals(1.0 * prefs.getNumCorrect('b', 'c', comp) / size, dist.cumulativeProbability(strDiff), tol);
		assertEquals(1.0 * prefs.getNumCorrect('c', 'd', comp) / size, dist.cumulativeProbability(strDiff), tol);
		
		assertEquals(1.0 * prefs.getNumCorrect('a', 'c', comp) / size, dist.cumulativeProbability(2*strDiff), tol);
		assertEquals(1.0 * prefs.getNumCorrect('b', 'd', comp) / size, dist.cumulativeProbability(2*strDiff), tol);
		
		assertEquals(1.0 * prefs.getNumCorrect('a', 'd', comp) / size, dist.cumulativeProbability(3*strDiff), tol);
	}

}
