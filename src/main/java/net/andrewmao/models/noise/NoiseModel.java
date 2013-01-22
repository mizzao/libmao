package net.andrewmao.models.noise;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Random;

import net.andrewmao.socialchoice.rules.PreferenceProfile;

public abstract class NoiseModel<T> {

	protected List<T> candidates;
	protected Random rnd;
	
	public NoiseModel(List<T> candidates, Random rnd) {
		this.candidates = candidates;
		this.rnd = rnd;
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

	public abstract PreferenceProfile<T> sampleProfile(int size);
	
	public abstract double logLikelihood(PreferenceProfile<T> profile);

}
