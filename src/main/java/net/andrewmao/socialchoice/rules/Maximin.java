package net.andrewmao.socialchoice.rules;

import java.util.Arrays;

import net.andrewmao.models.discretechoice.ScoredItems;

import org.apache.commons.lang.ArrayUtils;

public class Maximin extends ScoredVotingRule {
	
	@Override
	public <T> ScoredItems<T> getScoredRanking(PreferenceProfile<T> profile) {		
		T[] candidates = profile.getSortedCandidates();
		int[] maximin = getMaximinScores(profile);
		
		return new ScoredItems<T>(candidates, maximin);
	}

	<T> int[] getMaximinScores(PreferenceProfile<T> profile) {
		final T[] candidates = profile.getSortedCandidates();
		
		int[][] xOverY = new int[candidates.length][candidates.length];
		for( int[] row : xOverY) Arrays.fill(row, 0);
		
		for( T[] ranking : profile.profile ) {
			// Count the number of times each candidate is preferred to another
			for( int i = 0; i < ranking.length; i++ ) {
				for( int j = i+1; j < ranking.length; j++ ) {
					int idxI = ArrayUtils.indexOf(candidates, ranking[i]);
					int idxJ = ArrayUtils.indexOf(candidates, ranking[j]);
					
					// i is preferred to j since it came earlier in the ranking
					xOverY[idxI][idxJ]++;
				}
			}						
		}
		
//		System.out.println(Arrays.deepToString(xOverY));
		
		final int[] maximin = new int[candidates.length];
		
		int i = 0;
		for( int[] row : xOverY ) {			
			maximin[i] = Integer.MAX_VALUE;
					
			for( int j = 0; j < row.length; j++ ) {
				if( i == j ) continue; // ignore self scores, only count other scores
				
				if( row[j] < maximin[i]) maximin[i] = row[j];
			}
						
			i++;
		}		
		
		return maximin;
	}
}
