package net.andrewmao.models.noise;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import net.andrewmao.math.RandomGeneration;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

/**
 * Generates rankings with pairwise noise according to the Thurstone model,
 * using a standard normal distribution
 * 
 * @author mao
 *
 */
public class NormalNoiseModel<T> extends RandomUtilityModel<T> {

	public static final double THURSTONE_SIGMA = Math.sqrt(0.5);
	
	private final double[] sds;
	
	public NormalNoiseModel(List<T> candidates, double[] strengths) {
		super(candidates, strengths);
		
		sds = new double[candidates.size()];		
		for( int j = 0; j < candidates.size(); j++ ) 
			sds[j] = THURSTONE_SIGMA;
	}
	
	public NormalNoiseModel(List<T> candidates, double[] strengths, double[] sds) {
		super(candidates, strengths);
		
		this.sds = sds;
	}
	
	public NormalNoiseModel(List<T> ordering, double[] strengths, double stdev) {
		super(ordering, strengths);
		
		sds = new double[candidates.size()];		
		for( int j = 0; j < candidates.size(); j++ ) 
			sds[j] = stdev;
	}

	/**
	 * Initialized a fixed-variance probit model with the same difference between adjacent candidates
	 * After scaling, equivalent to Thurstone model.
	 * 
	 * @param stuffList
	 * @param random
	 * @param strDiff
	 * @param stdev
	 */
	public NormalNoiseModel(List<T> candidates, double adjStrDiff, double stdev) {
		super(candidates, adjStrDiff);
		
		sds = new double[candidates.size()];		
		for( int j = 0; j < candidates.size(); j++ ) 
			sds[j] = stdev;
	}

	/**
	 * Initializes a Thurstone model with the same difference between adjacent candidates
	 * @param candidates
	 * @param rnd
	 * @param adjStrDiff
	 */
	public NormalNoiseModel(List<T> candidates, double adjStrDiff) {
		// Generate normal random variables with variance 0.5
		this(candidates, adjStrDiff, THURSTONE_SIGMA);
	}

	@Override
	public String toParamString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(candidates.toString()).append("\n");
		sb.append(Arrays.toString(super.strParams)).append("\n");
		sb.append(Arrays.toString(sds));
		
		return sb.toString();	
	}

	@Override
	public PreferenceProfile<T> sampleProfile(int size, Random rnd) {
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
	
	public double[] getSigmas() {
		return sds;
	}

	@Override
	public double logLikelihood(PreferenceProfile<T> profile) {		
		return new NormalLogLikelihood(super.strParams, sds).logLikelihood(profile, candidates);		
	}

}
