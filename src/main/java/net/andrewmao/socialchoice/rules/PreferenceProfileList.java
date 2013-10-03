package net.andrewmao.socialchoice.rules;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.andrewmao.misc.Pair;

public class PreferenceProfileList<T> extends ArrayList<PreferenceProfile<T>> {

	private static final long serialVersionUID = 2624548982957801856L;
	
	private String name = null;
	
	public PreferenceProfileList(int numPartitions) {
		super(numPartitions);
	}

	public PreferenceProfileList(List<PreferenceProfile<T>> preferences) {
		super(preferences);
	}
	
	public void setString(String name) {
		this.name = name;
	}	

	public String toString() {
		return name == null ? super.toString() : name;
	}

	public T[] getCandidates() {
		return get(0).getSortedCandidates();				
				
	}

	public Map<Pair<T,T>, Double> getErrorRates(Comparator<T> comp) {
		T[] cands = getCandidates();
		Map<Pair<T,T>, Double> results = new HashMap<Pair<T, T>, Double>();
		
		for(int i = 0; i < cands.length; i++) {
			for( int j = i+1; j < cands.length; j++) {
				int correct = 0;
				int total = 0;
					
				for( PreferenceProfile<T> pp : this ) {
					correct += pp.getNumCorrect(cands[i], cands[j], comp);
					total += pp.getNumRankings();
				}							
				
				results.put(new Pair<T,T>(cands[i], cands[j]), 1.0d * correct / total);
			}
		}
		
		return results;
	}
	
	public PreferenceProfileList<T> reduceToSubsets(int subsetSize, Random rnd) {				
		PreferenceProfileList<T> shrunken = new PreferenceProfileList<T>(size());
		
		for( PreferenceProfile<T> profile : this ) {
			shrunken.add(profile.copyRandomSubset(subsetSize, rnd));
		}
		
		return shrunken;
	}
	
	public PreferenceProfile<T> concatenate() {
		int size = 0;
		for(PreferenceProfile<T> profile : this) {
			size += profile.profile.length;
		}
						
		@SuppressWarnings("unchecked")
		T[][] arr = (T[][]) Array.newInstance(this.get(0).profile.getClass().getComponentType(), size);
		// = new T[size][];
		
		int idx = 0;
		for( PreferenceProfile<T> profile : this) {
			System.arraycopy(profile.profile, 0, arr, idx, profile.profile.length);
			idx += profile.profile.length;
		}
		
		return new PreferenceProfile<T>(arr);
	}

	public static <T> PreferenceProfileList<T> singleton(
			PreferenceProfile<T> profile) {		
		return new PreferenceProfileList<T>(Collections.singletonList(profile));
	}
	
}
