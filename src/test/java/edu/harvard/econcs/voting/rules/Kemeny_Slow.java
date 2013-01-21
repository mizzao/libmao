package edu.harvard.econcs.voting.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import net.andrewmao.math.PermutationGenerator;
import net.andrewmao.math.RandomSelection;
import net.andrewmao.math.SmallPermutationGenerator;

/**
 * Slow version of Kemeny committed 11/11 revision 280
 * @author mao
 *
 */
public class Kemeny_Slow implements VotingRule {
	
	public String toString() { return this.getClass().getSimpleName(); }
	
	public <T> List<List<T>> getAllRankings(PreferenceProfile<T> prefs) {
		T[][] preferences = prefs.profile;
		 
		int c = prefs.getNumCandidates();
		T[] candidates = prefs.getSortedCandidates();		
		
		long fewestDisagreements = Long.MAX_VALUE;
		List<List<T>> bestRankings = new ArrayList<List<T>>();
		
		PermutationGenerator perm = new SmallPermutationGenerator(c);
		
		while( perm.hasMore() ) {
			int[] p = perm.getNext();						
			
			long disagreements = 0;
			
			for( T[] ranking : preferences ) {				
				// Check number of disagreements over all pairs of candidates
				for( int i = 0; i < c; i++ ) {
					for( int j = i+1; j < c; j++ ) {												
						int sign1 = ArrayUtils.indexOf(p, i) - 
								ArrayUtils.indexOf(p, j);
						int sign2 = ArrayUtils.indexOf(ranking, candidates[i]) - 
								ArrayUtils.indexOf(ranking, candidates[j]);
						
						if( Math.signum(sign1) != Math.signum(sign2) ) disagreements++;
					}
				}
			}
			
			if( disagreements < fewestDisagreements ) {
				fewestDisagreements = disagreements;
				bestRankings.clear();				
			}
			
			if( disagreements == fewestDisagreements ) {
				// Don't instantiate things unless we have to ... at the end
				List<T> thisRanking = new ArrayList<T>(4);
				for( int i = 0; i < p.length; i++ ) thisRanking.add(candidates[p[i]]);
				bestRankings.add(thisRanking);
			}			
		}
		
		return bestRankings;
	}
		
	@Override
	public <T> List<T> getRanking(PreferenceProfile<T> prefs) {
		// This returns a random ranking for now, instead of undetermined biases
		
		return RandomSelection.selectRandom(getAllRankings(prefs));
	}

}
