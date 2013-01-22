package net.andrewmao.models.noise;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import net.andrewmao.models.discretechoice.ScoredItems;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.apache.commons.math3.distribution.ExponentialDistribution;

/**
 * Gumbel noise model - the model for Plackett-Luce. 
 * @author mao
 *
 * @param <T>
 */
public class GumbelNoiseModel<T> extends RandomUtilityModel<T> {

	public GumbelNoiseModel(ScoredItems<T> scoreMap, Random rnd) {
		super(scoreMap, rnd);		
	}
	
	public GumbelNoiseModel(List<T> candidates, Random rnd, double[] strengths) {
		super(candidates, rnd, strengths);		
	}
	
	public GumbelNoiseModel(List<T> candidates, Random rnd, double adjStrDiff) {
		super(candidates, rnd, adjStrDiff);		
	}

	@Override
	public PreferenceProfile<T> sampleProfile(int size) {
		T[][] profile = super.getProfileArrayInitialized(size);		
		
		// Set up random number generators
		ExponentialDistribution[] dists = new ExponentialDistribution[candidates.size()];		
		for( int j = 0; j < candidates.size(); j++ ) 
		{			
			double mean = Math.exp(-strParams[j]);
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
		
	@Override	
	public double logLikelihood(PreferenceProfile<T> profile) {
		// The gumbel model log likelihood.
		double ll = 0;
		
		for( T[] preference : profile.getProfile() ) {
			double gammaSum = 0;
			for( int i = preference.length - 1; i >= 0; i-- ) {
				double gamma_i = strMap.get(preference[i]).doubleValue();
				gammaSum += gamma_i;
				if( i == preference.length - 1 ) continue;
				ll += Math.log(gamma_i / gammaSum);
			}
		}
		
		return ll;			
	}

}
