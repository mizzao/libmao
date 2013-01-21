package net.andrewmao.socialchoice.preferences;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.apache.commons.math3.distribution.ExponentialDistribution;


public class GumbelNoiseGenerator<T> extends PreferenceGenerator<T> {

	private final double strDiff;
	
	public GumbelNoiseGenerator(List<T> candidates, Random rnd, double strDiff) {
		super(candidates, rnd);
		this.strDiff = strDiff;
	}

	@Override
	public PreferenceProfile<T> getRandomProfile(int size) {
		T[][] profile = super.getProfileArrayInitialized(size);		
		
		ExponentialDistribution[] dists = new ExponentialDistribution[candidates.size()];
		
		for( int j = 0; j < candidates.size(); j++ ) 
		{
			double gamma = -j * strDiff;
			double mean = Math.exp(-gamma);
			dists[j] = new ExponentialDistribution(mean);
			dists[j].reseedRandomGenerator(rnd.nextLong());
		}				
		
		for( int i = 0; i < size; i++ ) {			
			final double[] strVals = new double[candidates.size()];			
			
			// Generate exponential random variables
			for( int j = 0; j < candidates.size(); j++ ) 						
				strVals[j] = dists[j].sample();										

			// Sort by the resulting strength parameters
			Arrays.sort(profile[i], new Comparator<T>() {
				@Override
				public int compare(T o1, T o2) {
					int i1 = candidates.indexOf(o1);
					int i2 = candidates.indexOf(o2);
					/* Reverse sort order - lower exponential comes first
					 * so it's the same as normal sort order
					 */
					return Double.compare(strVals[i1], strVals[i2]);
				}				
			});
		}
		
		return new PreferenceProfile<T>(profile);
	}

}
