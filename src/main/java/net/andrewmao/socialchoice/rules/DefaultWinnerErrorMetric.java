package net.andrewmao.socialchoice.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.andrewmao.models.discretechoice.ScoredItems;
 
/**
 * Implements the winner correct metric on comparables with a natural ordering 
 * @author mao
 *
 * @param <T>
 */
public class DefaultWinnerErrorMetric<T extends Comparable<T>> extends RankingMetric<T> {
	
	@Override
	public double compute(List<T> ranking) {				
		List<T> sorted = new ArrayList<T>(ranking);
		Collections.sort(sorted);
						
		if( sorted.get(0) == ranking.get(0) ) return 0;				
		else return 1;
	}

	@Override
	public double computeByScore(ScoredItems<T> scores) {
		List<T> keys = new ArrayList<T>(scores.keySet());
		
		double firstScore = scores.get(keys.get(0)).doubleValue();				
		int tiesForFirst = 1;
		
		for( int i = 1; i < keys.size(); i++ ) {
			double otherScore = scores.get(keys.get(i)).doubleValue();
			if( firstScore < otherScore )
				return 1;
			else if( firstScore == otherScore )
				tiesForFirst++;
		}
		
		// How many other candidates could possibly win?
		return (tiesForFirst - 1.0) / tiesForFirst;				
	}
}