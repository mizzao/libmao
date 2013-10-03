package net.andrewmao.socialchoice.rules;

import java.util.ArrayList;
import java.util.List;

import net.andrewmao.models.discretechoice.ScoredItems;

/**
 * Implements the Kendall tau error metric on naturally ordered comparables
 * @author mao
 *
 * @param <T>
 */
public class DefaultKendallTauMetric<T extends Comparable<T>> extends RankingMetric<T> {
	
	@Override
	public double compute(List<T> ranking) {				
		double mistakes = 0;
		
		for( int i = 0; i < ranking.size(); i++ ) {
			for( int j = i+1; j < ranking.size(); j++ ) {
				/* Mistake if i before j but it's supposed to come afterward
				 * Half a mistake for 
				 */
				int compare = ranking.get(i).compareTo(ranking.get(j));
				
				if( compare > 0 ) mistakes += 1;				
			}
		}
		
		return mistakes;
	}

	@Override
	public double computeByScore(ScoredItems<T> scores) {
		/*
		 * Expected random tie breaking:
		 * If two alternatives are ranked equally then that is half a mistake
		 * 
		 * If three alternatives are ranked equally then that is 1.5 mistakes
		 * (can be computed just over pairs as above)
		 * 
		 * If four alternatives are ranked equally, 3 mistakes (and so on)
		 */
		double mistakes = 0;
		
		List<T> keys = new ArrayList<T>(scores.keySet());
		for( int i = 0; i < keys.size(); i++ ) {
			for( int j = i + 1; j < keys.size(); j++) {
				int compare = 
						new Double(
								scores.get(keys.get(i)).doubleValue()).compareTo(
										scores.get(keys.get(j)).doubleValue()); 
				if( compare < 0 )
					mistakes += 1;
				else if( compare == 0 )
					mistakes += 0.5;
			}
		}
		
		return mistakes;
	}
}