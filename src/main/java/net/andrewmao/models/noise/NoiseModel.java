package net.andrewmao.models.noise;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Random;

import net.andrewmao.socialchoice.rules.PreferenceProfile;
import net.andrewmao.socialchoice.rules.SocialChoiceMetric;

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

	public abstract PreferenceProfile<T> sampleProfile(int size, Random rnd);
	
	public abstract double computeMetric(SocialChoiceMetric<T> metric);
	
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
