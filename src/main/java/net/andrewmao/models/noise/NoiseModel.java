package net.andrewmao.models.noise;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Random;

import net.andrewmao.socialchoice.rules.PreferenceProfile;
import net.andrewmao.socialchoice.rules.RankingMetric;

public abstract class NoiseModel<T> {

	protected List<T> candidates;
	protected Double fittedLikelihood = null;
	
	public NoiseModel(List<T> candidates) {
		this.candidates = candidates;		
	}

	@SuppressWarnings("unchecked")
	protected T[][] getProfileArray( int size ) {		
		int[] dimensions = new int[] { size, candidates.size() };		
		
		return (T[][]) Array.newInstance(candidates.get(0).getClass(), dimensions);
	}
	
	protected T[][] getProfileArrayInitialized(int size) {
		T[][] profile = getProfileArray(size);
		
		for(T[] ranking : profile) candidates.toArray(ranking);		
		return profile;
	}	

	/**
	 * Sample a preference profile from this model
	 * @param size
	 * @param rnd
	 * @return
	 */
	public abstract PreferenceProfile<T> sampleProfile(int size, Random rnd);
	
	/**
	 * Compute the goodness of this model by some ranking metric
	 * @param metric
	 * @return
	 */
	public abstract double computeMetric(RankingMetric<T> metric);
	
	/**
	 * The probability of one candidate beating another under this noise model.
	 * @param winner
	 * @param loser
	 * @return
	 */
	public abstract double marginalProbability(T winner, T loser);
	
	public void setFittedLikelihood(double ll) {
		fittedLikelihood = ll;
	}
	
	public Double getFittedLikelihood() {
		return fittedLikelihood;
	}
	
	public abstract double logLikelihood(PreferenceProfile<T> profile);

	/**
	 * Serialize the model to a string.
	 * @return
	 */
	public abstract String toParamString();

}
