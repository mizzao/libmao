package net.andrewmao.models.noise;

import java.util.Arrays;
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

	public GumbelNoiseModel(ScoredItems<T> scoreMap) {
		super(scoreMap);		
	}
	
	public GumbelNoiseModel(List<T> candidates, double[] strengths) {
		super(candidates, strengths);		
	}
	
	public GumbelNoiseModel(List<T> candidates, double adjStrDiff) {
		super(candidates, adjStrDiff);		
	}

	@Override
	public String toParamString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(candidates.toString()).append("\n");
		sb.append(Arrays.toString(super.strParams));		
		
		return sb.toString();	
	}

	@Override
	public double[] sampleUtilities(Random rnd) {		
		throw new UnsupportedOperationException("Sampling is currently done with exponentials");
	}

	@Override
	public PreferenceProfile<T> sampleProfile(int size, Random rnd) {
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
			double[] strVals = new double[candidates.size()];			
			
			// Generate exponential random variables
			for( int j = 0; j < candidates.size(); j++ ) 						
				strVals[j] = dists[j].sample();										

			// Sort by the resulting strength parameters
			sortByStrengthReverse(profile[i], strVals);
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
				gammaSum += Math.exp(gamma_i);
				if( i == preference.length - 1 ) continue;
				ll += gamma_i;
				ll -= Math.log(gammaSum);
			}
		}
		
		return ll;			
	}

}
