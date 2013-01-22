package net.andrewmao.models.noise;

import java.util.List;
import java.util.Random;


import net.andrewmao.math.RandomSelection;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

/**
 * Generates preference profiles with an average of X flips per ranking
 * @author mao
 *
 * @param <T>
 */
public class FlipNoiseModel<T> extends NoiseModel<T> {

	final double flipFactor;
	
	public FlipNoiseModel(List<T> candidates, Random rnd, double flipFactor) {
		super(candidates, rnd);
		
		this.flipFactor = flipFactor;
	}

	@Override
	public PreferenceProfile<T> sampleProfile(int size) {
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
	
}
