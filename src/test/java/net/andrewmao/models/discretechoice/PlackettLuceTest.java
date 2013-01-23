package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import net.andrewmao.math.RandomGeneration;
import net.andrewmao.models.noise.GumbelNoiseModel;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

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
		int m = 4;
		Random rnd = new Random();
		
		Character[] stuff = new Character[] { 'A', 'B', 'C', 'D' };
		final List<Character> stuffList = Arrays.asList(stuff);
		
		double[] means = new double[] {0, -1, -2, -3};
		double[] sds = new double[] {1, 1, 1, 1};
		
		PlackettLuceModel plmm = new PlackettLuceModel();
		
		Character[][] profile = new Character[n][m];
		for( int i = 0; i < n; i++) {
			final double[] vals = RandomGeneration.gaussianArray(means, sds, rnd);
			profile[i] = Arrays.copyOf(stuff, stuff.length);
			
			Arrays.sort(profile[i], new Comparator<Character>() {
				@Override
				public int compare(Character o1, Character o2) {
					int i1 = stuffList.indexOf(o1);
					int i2 = stuffList.indexOf(o2);
					// Higher strength parameter comes earlier in the array
					return Double.compare(vals[i2], vals[i1]);
				}				
			});			
		}		
		
		PreferenceProfile<Character> prefs = new PreferenceProfile<Character>(profile);
		
		GumbelNoiseModel<Character> model = plmm.fitModel(prefs);
		ScoredItems<Character> params = model.getValueMap();
				
		List<Character> ranking = params.getRanking();
		
		System.out.println(params);
		System.out.println(ranking);
				
		assertEquals(stuffList, ranking);
	}

}
