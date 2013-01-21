package net.andrewmao.math;

import java.util.Random;

public class RandomGeneration {

	public static double[] gaussianArray(double[] means, double[] sds, Random rnd) {
		double[] arr = new double[means.length];
		
		for( int i = 0; i < arr.length; i++ )
			arr[i] = rnd.nextGaussian() * sds[i] + means[i];
		
		return arr;
	}
}
