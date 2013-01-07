package net.andrewmao.models.randomutility;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import net.andrewmao.math.RandomGeneration;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NormalMCEMTest {

	Random rnd;
	
	@Before
	public void setUp() throws Exception {
		rnd = new Random();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		int n = 500;
		int iters = 30;
		double abseps = 1e-8;
		double releps = 1e-5;
		
		Character[] stuff = new Character[] { 'A', 'B', 'C', 'D' };
		final List<Character> stuffList = Arrays.asList(stuff);
		
		double[] means = new double[] {0, -1, -2, -3};
		double[] sds = new double[] {1, 1, 1, 1};
		
		NormalMCEM<Character> model = new NormalMCEM<Character>(stuffList);
		
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
		
		model.setup(new NormalDistribution(0,1).sample(4), iters, abseps, releps);
		
		double[] fitted = model.getParameters();
		
		System.out.println(Arrays.toString(fitted));
		
		// This assertion must have first element of means be 0, and variances adjusted to 1
		assertArrayEquals(means, fitted, 1e-1);
	}
	
}
