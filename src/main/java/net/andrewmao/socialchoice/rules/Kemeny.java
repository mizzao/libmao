package net.andrewmao.socialchoice.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import net.andrewmao.math.PermutationGenerator;
import net.andrewmao.math.RandomSelection;
import net.andrewmao.math.SmallPermutationGenerator;

public class Kemeny implements VotingRule {
	
	public String toString() { return this.getClass().getSimpleName(); }
	
	public <T> List<List<T>> getAllRankings(PreferenceProfile<T> prefs) {
		T[][] preferences = prefs.profile;
		 
		int c = prefs.getNumCandidates();
		T[] candidates = prefs.getSortedCandidates();		
		
		long fewestDisagreements = Long.MAX_VALUE;
		List<int[]> bestRankings = new ArrayList<int[]>();
		
		// Compute pairwise preferences in the preference profile, relative to some arbitrary index
		int[][] iOverJ = new int[c][c];
		
		for( T[] ranking : preferences ) {
			for( int i = 0; i < c; i++ ) {
				for( int j = i+1; j < c; j++ ) {
					// If i is ranked higher than j in this ranking, increment pairwise count								
					if( ArrayUtils.indexOf(ranking, candidates[i]) < 
							ArrayUtils.indexOf(ranking, candidates[j]) )
						iOverJ[i][j]++;
					else
						iOverJ[j][i]++;
				}
			}
		}		
		
		PermutationGenerator perm = new SmallPermutationGenerator(c);
		
		while( perm.hasMore() ) {
			int[] p = perm.getNext();						
			
			long disagreements = 0;
			
			// Compute total disagreements for this permutation
			for( int i = 0; i < c; i++ ) {
				for( int j = i+1; j < c; j++ ) {
					if( ArrayUtils.indexOf(p, i) < ArrayUtils.indexOf(p, j) )
						disagreements += iOverJ[j][i];
					else
						disagreements += iOverJ[i][j];
				}
			}
			
			if( disagreements < fewestDisagreements ) {
				fewestDisagreements = disagreements;
				bestRankings.clear();				
			}
			
			if( disagreements == fewestDisagreements ) {
				// Don't instantiate things unless we have to ... at the end
				bestRankings.add(p.clone());
			}			
		}
		
		// Instantiate all actual rankings
		List<List<T>> ret = new ArrayList<List<T>>(bestRankings.size());
		for( int[] p : bestRankings ) {
			List<T> thisRanking = new ArrayList<T>(c);
			for( int i = 0; i < p.length; i++ ) 
				thisRanking.add(candidates[p[i]]);
			ret.add(thisRanking);
		}		
		return ret;
	}
		
	@Override
	public <T> List<T> getRanking(PreferenceProfile<T> prefs) {
		// This returns a random ranking for now, instead of undetermined biases
		
		return RandomSelection.selectRandom(getAllRankings(prefs));
	}

}
