package net.andrewmao.socialchoice.rules;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;

import net.andrewmao.math.RandomSelection;

public class PreferenceProfile<T> {

	T[][] profile;

	public PreferenceProfile(T[][] profile) {
		this.profile = profile;
	}
	
	public T[][] getProfile() {
		return profile;
	}

	public T[] getSortedCandidates() {
		T[] candidates = Arrays.copyOf(profile[0], profile[0].length);
		
		Arrays.sort(candidates);
		
		return candidates;
	}

	public int getNumCandidates() {
		return profile[0].length;
	}
	
	public int getNumRankings() {		
		return profile.length;
	}

	public Map<T, int[]> getCounts() {
		Map<T, int[]> counts = new TreeMap<T, int[]>();
		
		for( T t : profile[0] ) counts.put(t, new int[profile[0].length]);

		for( T[] ranking : profile ) {
			for( int i = 0; i < ranking.length; i++ )
				counts.get(ranking[i])[i]++;
		}
		
		return counts;
	}
	
	/**
	 * Check how many of the actual pairwise rankings agree with the comparator.
	 * @param first
	 * @param second
	 * @param comp
	 * @return
	 */
	public int getNumCorrect(T first, T second, Comparator<T> comp) {
		int correct = 0;
		
		for( T[] ranking : profile ) {
			int idxFirst = ArrayUtils.indexOf(ranking, first);
			int idxSecond = ArrayUtils.indexOf(ranking, second);
			
			if( idxFirst == ArrayUtils.INDEX_NOT_FOUND || idxSecond == ArrayUtils.INDEX_NOT_FOUND )
				continue;
			
			int c = comp.compare(first, second);
			
			if( c < 0 && idxFirst < idxSecond )
				correct++;
			else if( c > 0 && idxFirst > idxSecond )
				correct++;
			else if( c == 0 && idxFirst == idxSecond )
				correct++;
		}
		
		return correct;
	}

	/**
	 * reduces the preference profile to a smaller subsample
	 * @param subsetSize
	 */
	public PreferenceProfile<T> copyRandomSubset(int subsetSize, Random rnd) {
		if( subsetSize < profile.length )
			return new PreferenceProfile<T>(RandomSelection.selectKRandom(profile, subsetSize, rnd));
		else return this;
	}
	
	/**
	 * Returns a candidate if it is always ranked highest in the preference profile
	 * @return
	 */
	public T getConstantWinner() {
		T winner = profile[0][0];
		
		for( int i = 1; i < profile.length; i++ ) {
			if( !winner.equals(profile[i][0]) ) return null;
		}
		
		return winner;
	}

	/**
	 * Returns a candidate if it is always ranked lowest in the preference profile
	 * @return
	 */
	public T getConstantLoser() {
		int c = getNumCandidates();
		
		T loser = profile[0][c-1];
		
		for( int i = 1; i < profile.length; i++ ) {
			if( !loser.equals(profile[i][c-1]) ) return null;
		}
		
		return loser;		
	}

	/**
	 * Creates a copy of this preference profile removing any constant winner or loser at the ends.
	 * Used for MM models to ensure convergence.
	 * @return
	 */
	public PreferenceProfile<T> preprocess() {
		boolean removeWinner = getConstantWinner() != null;
		boolean removeLoser = getConstantLoser() != null;
		
		int newSize = getNumCandidates();
		if( removeWinner ) newSize--;
		if( removeLoser ) newSize--;
		
		@SuppressWarnings("unchecked")
		T[][] newProfile = (T[][]) Array.newInstance(profile[0][0].getClass(), profile.length, newSize);
		int startIdx = removeWinner ? 1 : 0;		
		
		for( int i = 0; i < profile.length; i++ )
			System.arraycopy(profile[i], startIdx, newProfile[i], 0, newSize);		
		
		return new PreferenceProfile<T>(newProfile);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for( T[] ranking : profile ) {
			sb.append(Arrays.toString(ranking));
			sb.append("\n");
		}
		return sb.toString();
	}

	public PreferenceProfile<T> slice(int fromIdx, int toIdx) {
		int newSize = toIdx - fromIdx;
		@SuppressWarnings("unchecked")
		T[][] newProfile = (T[][]) Array.newInstance(profile[0][0].getClass(), profile.length, newSize);
		
		for( int i = 0; i < profile.length; i++ )
			System.arraycopy(profile[i], fromIdx, newProfile[i], 0, newSize);				
		
		return new PreferenceProfile<T>(newProfile);
	}
	
}
