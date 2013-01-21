package net.andrewmao.math;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

public class WeightedSelectionTest {
	
	Random rnd = new Random();

	@Test
	public void testSelectRandomWeighted() {
		double[] wts;
		int idx;
		
		wts = new double[] { 0.0, 1.0 };
		idx = RandomSelection.selectRandomWeighted(wts, rnd);		
		assertEquals(idx, 1);
		
		wts = new double[] { 1.0, 0.0 };
		idx = RandomSelection.selectRandomWeighted(wts, rnd);
		assertEquals(idx, 0);
	}
	
	@Test
	public void testSelectRandomWeighted1() {
		double[] wts = new double[] { 1.0, 2.0, 3.0 };
		
		int tr = 10000;
		int[] idxs = new int[3];
		double tol = 1e-2;
		
		for( int i = 0; i < tr; i++ ) {
			idxs[RandomSelection.selectRandomWeighted(wts, rnd)]++;
		}
		
		System.out.println(Arrays.toString(idxs));
		
		assertTrue(Math.abs(1.0 * idxs[0] / tr - 1.0 / 6.0) < tol);
		assertTrue(Math.abs(1.0 * idxs[1] / tr - 2.0 / 6.0) < tol);
		assertTrue(Math.abs(1.0 * idxs[2] / tr - 3.0 / 6.0) < tol);				
		
	}

}
