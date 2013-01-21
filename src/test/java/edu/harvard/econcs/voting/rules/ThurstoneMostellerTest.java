package edu.harvard.econcs.voting.rules;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import net.andrewmao.models.discretechoice.ScoredItems;

import edu.harvard.econcs.voting.preferences.PreferenceGenerator;
import edu.harvard.econcs.voting.preferences.ThurstoneNoiseGenerator;
import edu.harvard.econcs.voting.rules.PreferenceProfile;
import edu.harvard.econcs.voting.rules.ThurstoneMostellerRule;

public class ThurstoneMostellerTest {

	@Test
	public void test() {
		
		List<Integer> stuff = Arrays.asList(new Integer[] {1, 2, 3, 4});
		
		PreferenceGenerator<Integer> rfg = new ThurstoneNoiseGenerator<Integer>(stuff, new Random(), 0.2);
		
		PreferenceProfile<Integer> generated = rfg.getRandomProfile(1000);
		
		ThurstoneMostellerRule tmr = new ThurstoneMostellerRule(true);
		
		ScoredItems<Integer> map = tmr.getScoredRanking(generated);
		List<Integer> result = map.getRanking();
		
		System.out.println(map);
		System.out.println(result);
		
		assertEquals(stuff, result);
	}

}
