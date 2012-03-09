/**
 * 
 */
package net.andrewmao.math;

import junit.framework.TestCase;

import org.apache.commons.math.stat.descriptive.moment.*;

/**
 * @author mao
 *
 */
public class ExponentialTest extends TestCase {

	public void testDistribution() {
		// Sampling params
		int sampleSize = 100;		
		double stdError = 1.0 / Math.sqrt(sampleSize);
		
		// Exponential dist params
		double lambda = 2;
		double ilambda = 1 / lambda;		
		
		Exponential exp = new Exponential(lambda);
		
		double[] nums = new double[sampleSize];
		
		for( int i = 0; i < sampleSize; i++ )
			nums[i] = exp.sample();
	
		Mean m = new Mean();
		double mean = m.evaluate(nums);		
		
		StandardDeviation sd = new StandardDeviation();
		double stdev = sd.evaluate(nums);
		
		System.out.printf("Exponential distribution: Lambda: %f, Sample Mean: %f, Sample stdev: %f",
				lambda, mean, stdev);
		System.out.println();
		
		// TODO check the math for this error calculation
		assertEquals( mean, ilambda, stdError );						
		assertEquals( stdev, ilambda, 2 * stdError );
	}
	
}
