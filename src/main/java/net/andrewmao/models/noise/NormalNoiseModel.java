package net.andrewmao.models.noise;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import net.andrewmao.math.RandomGeneration;
import net.andrewmao.models.discretechoice.ScoredItems;
import net.andrewmao.socialchoice.rules.PreferenceProfile;


/**
 * Generates rankings with pairwise noise according to the Thurstone model,
 * using a standard normal distribution
 * 
 * @author mao
 *
 */
public class NormalNoiseModel<T> extends RandomUtilityModel<T> {

	private static final double THURSTONE_SIGMA = Math.sqrt(0.5);
	
	private final double[] sds;
	
	public NormalNoiseModel(ScoredItems<T> scoreMap, Random rnd) {
		super(scoreMap, rnd);
		
		sds = new double[candidates.size()];		
		for( int j = 0; j < candidates.size(); j++ ) 
			sds[j] = THURSTONE_SIGMA;
	}
	
	public NormalNoiseModel(List<T> candidates, Random rnd, double[] strengths, double[] sds) {
		super(candidates, rnd, strengths);
		
		this.sds = sds;
	}
	
	/**
	 * Initializes a Thurstone model with the same difference between adjacent candidates
	 * @param candidates
	 * @param rnd
	 * @param adjStrDiff
	 */
	public NormalNoiseModel(List<T> candidates, Random rnd, double adjStrDiff) {
		super(candidates, rnd, adjStrDiff);
		
		// Generate normal random variables with variance 0.5
		sds = new double[candidates.size()];		
		for( int j = 0; j < candidates.size(); j++ ) 
			sds[j] = THURSTONE_SIGMA;
	}

	@Override
	public PreferenceProfile<T> sampleProfile(int size) {
		T[][] profile = super.getProfileArrayInitialized(size);							
		
		for( int i = 0; i < size; i++ ) {																	
			final double[] strVals = RandomGeneration.gaussianArray(strParams, sds, rnd);

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

	@Override
	public double logLikelihood(PreferenceProfile<T> profile) {
		throw new UnsupportedOperationException();
	}

}
