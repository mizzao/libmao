package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import net.andrewmao.models.discretechoice.ScoredItems;
import net.andrewmao.models.noise.NoiseModel;
import net.andrewmao.models.noise.NormalNoiseModel;
import net.andrewmao.socialchoice.rules.PreferenceProfile;
import net.andrewmao.socialchoice.rules.ThurstoneMostellerRule;


public class ThurstoneMostellerTest {

	@Test
	public void test() {
		
		List<Integer> stuff = Arrays.asList(new Integer[] {1, 2, 3, 4});
		
		NoiseModel<Integer> rfg = new NormalNoiseModel<Integer>(stuff, 0.2);
		
		PreferenceProfile<Integer> generated = rfg.sampleProfile(1000, new Random());
		
		ThurstoneMostellerRule tmr = new ThurstoneMostellerRule(true);
		
		ScoredItems<Integer> map = tmr.getScoredRanking(generated);
		List<Integer> result = map.getRanking();
		
		System.out.println(map);
		System.out.println(result);
		
		assertEquals(stuff, result);
	}

}
