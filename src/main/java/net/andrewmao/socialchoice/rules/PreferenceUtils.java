package net.andrewmao.socialchoice.rules;

import java.lang.reflect.Array;

public class PreferenceUtils {
	
	/**
	 * Returns a candidate if it is always ranked highest in the preference profile
	 * @return
	 */
	public static <T> T getConstantWinner(PreferenceProfile<T> prefs) {		
		T winner = prefs.profile[0][0];
		
		for( int i = 1; i < prefs.profile.length; i++ ) {
			if( !winner.equals(prefs.profile[i][0]) ) return null;
		}
		
		return winner;
	}

	/**
	 * Returns a candidate if it is always ranked lowest in the preference profile
	 * @return
	 */
	public static <T> T getConstantLoser(PreferenceProfile<T> prefs) {
		int c = prefs.getNumCandidates();
		
		T loser = prefs.profile[0][c-1];
		
		for( int i = 1; i < prefs.profile.length; i++ ) {
			if( !loser.equals(prefs.profile[i][c-1]) ) return null;
		}
		
		return loser;		
	}
	
	/**
	 * Creates a copy of this preference profile removing any constant winner or loser at the ends.
	 * Used for MM models to ensure convergence.
	 * @return
	 */
	public static <T> PreferenceProfile<T> preprocess(PreferenceProfile<T> prefs) {
		boolean removeWinner = getConstantWinner(prefs) != null;
		boolean removeLoser = getConstantLoser(prefs) != null;
		
		int newSize = prefs.getNumCandidates();
		if( removeWinner ) newSize--;
		if( removeLoser ) newSize--;
		
		@SuppressWarnings("unchecked")
		T[][] newProfile = (T[][]) Array.newInstance(prefs.profile[0][0].getClass(), prefs.profile.length, newSize);
		int startIdx = removeWinner ? 1 : 0;		
		
		for( int i = 0; i < prefs.profile.length; i++ )
			System.arraycopy(prefs.profile[i], startIdx, newProfile[i], 0, newSize);		
		
		return new PreferenceProfile<T>(newProfile);
	}
}
