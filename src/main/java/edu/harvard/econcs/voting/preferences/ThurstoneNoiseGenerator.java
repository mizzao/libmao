package edu.harvard.econcs.voting.preferences;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import net.andrewmao.math.RandomGeneration;

import edu.harvard.econcs.voting.rules.PreferenceProfile;

/**
 * Generates rankings with pairwise noise according to the Thurstone model,
 * using a standard normal distribution
 * 
 * @author mao
 *
 */
public class ThurstoneNoiseGenerator<T> extends PreferenceGenerator<T> {

	private static final double SIGMA = Math.sqrt(0.5);
	
	private final double strDiff;
	
	public ThurstoneNoiseGenerator(List<T> candidates, Random rnd, double strDiff) {
		super(candidates, rnd);
		this.strDiff = strDiff;
	}

	@Override
	public PreferenceProfile<T> getRandomProfile(int size) {
		T[][] profile = super.getProfileArrayInitialized(size);		
		
		for( int i = 0; i < size; i++ ) {
			
			double[] means = new double[candidates.size()];
			double[] sds = new double[candidates.size()];
			
			// Generate normal random variables with difference in mean strDiff and variance 0.5
			for( int j = 0; j < candidates.size(); j++ ) 
			{
				means[j] = -j * strDiff;
				sds[j] = SIGMA;				
			}
			
			final double[] strVals = RandomGeneration.gaussianArray(means, sds, rnd);

			// Sort by the resulting strength parameters
			Arrays.sort(profile[i], new Comparator<T>() {
				@Override
				public int compare(T o1, T o2) {
					int i1 = candidates.indexOf(o1);
					int i2 = candidates.indexOf(o2);
					// Higher strength parameter comes earlier in the array
					return Double.compare(strVals[i2], strVals[i1]);
				}				
			});
		}
		
		return new PreferenceProfile<T>(profile);
	}

}
