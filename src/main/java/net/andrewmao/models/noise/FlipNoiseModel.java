package net.andrewmao.models.noise;

import java.util.List;
import java.util.Random;


import net.andrewmao.math.RandomSelection;
import net.andrewmao.socialchoice.rules.PreferenceProfile;
import net.andrewmao.socialchoice.rules.SocialChoiceMetric;

/**
 * Generates preference profiles with an average of X flips per ranking
 * @author mao
 *
 * @param <T>
 */
public class FlipNoiseModel<T> extends NoiseModel<T> {

	final double flipFactor;
	
	public FlipNoiseModel(List<T> candidates, double flipFactor) {
		super(candidates);
		
		this.flipFactor = flipFactor;
	}

	@Override
	public PreferenceProfile<T> sampleProfile(int size, Random rnd) {
		T[][] profile = super.getProfileArrayInitialized(size);
		
		int flips = (int) Math.round(flipFactor * size);
		
		int[] randIndices = RandomSelection.randomSeededMultiset(flips, size, rnd.nextLong());
				
		for(int f = 0; f < flips; f++) {
			// Pick a ranking to switch
			T[] ranking = profile[randIndices[f]];
			// Pick an index to swap
			int idx = rnd.nextInt(ranking.length - 1);
			
			T temp = ranking[idx];
			ranking[idx] = ranking[idx+1];
			ranking[idx+1] = temp;
		}
				
		return new PreferenceProfile<T>(profile);
	}

	@Override
	public double logLikelihood(PreferenceProfile<T> profile) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double computeMetric(SocialChoiceMetric<T> metric) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toParamString() {
		throw new UnsupportedOperationException();
	}
	
}
