package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ScoredItemsTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRanking() {
		
		Character[] items = new Character[] { 'A', 'B', 'C', 'D' };
		double[] scores = new double[] { 0.2, 0.1, 0.3, 0.0 };
		
		ScoredItems<Character> scored = new ScoredItems<Character>(items, scores);
		
		List<Character> expected = Arrays.asList(new Character[] { 'C', 'A', 'B', 'D' });
		
		assertEquals(expected, scored.getRanking());		
	}

}
