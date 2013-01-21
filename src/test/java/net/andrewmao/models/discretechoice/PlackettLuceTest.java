package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import net.andrewmao.math.RandomGeneration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PlackettLuceTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		int n = 50;			
		Random rnd = new Random();
		
		Character[] stuff = new Character[] { 'A', 'B', 'C', 'D' };
		final List<Character> stuffList = Arrays.asList(stuff);
		
		double[] means = new double[] {0, -1, -2, -3};
		double[] sds = new double[] {1, 1, 1, 1};
		
		PlackettLuceModel<Character> model = new PlackettLuceModel<Character>(stuffList);
		
		for( int i = 0; i < n; i++) {
			final double[] vals = RandomGeneration.gaussianArray(means, sds, rnd);
			Character[] copy = Arrays.copyOf(stuff, stuff.length);
			
			Arrays.sort(copy, new Comparator<Character>() {
				@Override
				public int compare(Character o1, Character o2) {
					int i1 = stuffList.indexOf(o1);
					int i2 = stuffList.indexOf(o2);
					// Higher strength parameter comes earlier in the array
					return Double.compare(vals[i2], vals[i1]);
				}				
			});
			
			model.addData(copy);
		}		
		
		ScoredItems<Character> fitted = model.getParameters();
		List<Character> ranking = fitted.getRanking();
		
		System.out.println(fitted);
		System.out.println(ranking);
				
		assertEquals(stuffList, ranking);
	}

}
