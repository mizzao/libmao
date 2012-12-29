/**
 * 
 */
package net.andrewmao.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import junit.framework.TestCase;


/**
 * @author mao
 *
 */
public class RandomSelectionTest extends TestCase {

	public void testStreamSelection() {
		
		int bins = 10;
		int expectedPerBin = 10;
		
		int samples = bins * expectedPerBin;
		
		double stdError = Math.sqrt(expectedPerBin);
		
		List<Integer> nums = new ArrayList<Integer>(bins);
		
		double[] binCounts = new double[bins];
		
		for( int i = 0; i < bins; i++ ) {
			nums.add(i);
			binCounts[i] = 0;
		}
			
		for( int j = 0; j < samples; j++) {
			binCounts[RandomSelection.selectRandom(nums)] += 1;
		}		
		
		StandardDeviation sd = new StandardDeviation();
		double stdev = sd.evaluate(binCounts);
		
		System.out.print( "Random Selection bins: " + Arrays.toString(binCounts) + " stdev ");		
		System.out.println(stdev);
		
		assertEquals(stdev, 0.0, 2 * stdError );
	}
	
}
