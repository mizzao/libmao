package edu.harvard.econcs.voting.rules;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.andrewmao.models.discretechoice.ScoredItems;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.harvard.econcs.voting.preferences.PreferenceGenerator;
import edu.harvard.econcs.voting.preferences.ThurstoneNoiseGenerator;

public class BradleyTerryTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		
		List<Integer> stuff = Arrays.asList(new Integer[] {1, 2, 3, 4});
		
		PreferenceGenerator<Integer> rfg = new ThurstoneNoiseGenerator<Integer>(stuff, new Random(), 0.2);
		
		PreferenceProfile<Integer> generated = rfg.getRandomProfile(100);
		
		BradleyTerryRule tmr = new BradleyTerryRule(true);
		
		ScoredItems<Integer> map = tmr.getScoredRanking(generated);
		List<Integer> result = map.getRanking();
		
		System.out.println(map);
		System.out.println(result);
		
		assertEquals(stuff, result);
	}

}
