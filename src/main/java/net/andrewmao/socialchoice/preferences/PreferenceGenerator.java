package net.andrewmao.socialchoice.preferences;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Random;

import net.andrewmao.socialchoice.rules.PreferenceProfile;


public abstract class PreferenceGenerator<T> {

	protected List<T> candidates;
	protected Random rnd;
	
	public PreferenceGenerator(List<T> candidates, Random rnd) {
		this.candidates = candidates;
		this.rnd = rnd;
	}

	@SuppressWarnings("unchecked")
	protected T[][] getProfileArray( int size ) {		
		int[] dimensions = new int[] { size, candidates.size() };		
		
		return (T[][]) Array.newInstance(candidates.get(0).getClass(), dimensions);
	}
	
	public T[][] getProfileArrayInitialized(int size) {
		T[][] profile = getProfileArray(size);
		
		for(T[] ranking : profile) candidates.toArray(ranking);
		
		return profile;
	}

	public abstract PreferenceProfile<T> getRandomProfile(int size);

}
