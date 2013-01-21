package net.andrewmao.socialchoice.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import net.andrewmao.math.PermutationGenerator;
import net.andrewmao.math.RandomSelection;
import net.andrewmao.math.SmallPermutationGenerator;
import net.andrewmao.socialchoice.rules.PreferenceProfile;
import net.andrewmao.socialchoice.rules.VotingRule;

/**
 * Oldest version of kemeny 10/11 revision 267 
 * used to only return 1 ranking
 * @author mao
 *
 */
public class Kemeny_Old implements VotingRule {
	
	public String toString() { return this.getClass().getSimpleName(); }
		
	public <T> List<List<T>> getAllRankings(PreferenceProfile<T> prefs) {
		T[][] preferences = prefs.profile;
		 
		int c = prefs.getNumCandidates();
		T[] candidates = prefs.getSortedCandidates();		
		
		long fewestDisagreements = Long.MAX_VALUE;		
		
		PermutationGenerator perm = new SmallPermutationGenerator(c);
		List<List<T>> bestRankings = new ArrayList<List<T>>();
		
		while( perm.hasMore() ) {
			int[] p = perm.getNext();
			
			T[] thisRanking = Arrays.copyOf(candidates, c);
			
			for( int i = 0; i < c; i++ ) thisRanking[i] = candidates[p[i]];
//			System.out.print(thisAsList);
			
			long disagreements = kendallTau(preferences, thisRanking);
			
			if( disagreements < fewestDisagreements ) {
				fewestDisagreements = disagreements;
				bestRankings.clear();				
			}
			
			if( disagreements == fewestDisagreements ) {
				// Don't instantiate things unless we have to ... at the end					
				bestRankings.add(Arrays.asList(thisRanking));
			}			
		}
		
		return bestRankings;
	}

	public static <T> long kendallTau(T[][] preferences, T[] someRanking) {
		int c = someRanking.length;
		
		long disagreements = 0;
		
		for( T[] ranking : preferences ) {				
			// Check number of disagreements over all pairs of candidates
			for( int i = 0; i < c; i++ ) {
				for( int j = i+1; j < c; j++ ) {
					T ci = someRanking[i];
					T cj = someRanking[j];
					
					int sign1 = i - j;
					int sign2 = 
							ArrayUtils.indexOf(ranking, ci) - 
							ArrayUtils.indexOf(ranking, cj);
					
					if( (sign1 < 0) && (sign2 > 0) ||
							(sign1 > 0) && (sign2 < 0) )
						disagreements++;
				}
			}
		}
		return disagreements;
	}
		
	@Override
	public <T> List<T> getRanking(PreferenceProfile<T> prefs) {
		// This returns a random ranking for now, instead of undetermined biases
		
		return RandomSelection.selectRandom(getAllRankings(prefs));
	}

}
