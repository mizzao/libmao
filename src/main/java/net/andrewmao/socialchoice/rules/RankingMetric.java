package net.andrewmao.socialchoice.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.andrewmao.models.discretechoice.ScoredItems;

public abstract class RankingMetric<T> {
	
	public abstract double compute(List<T> ranking);	
	public abstract double computeByScore(ScoredItems<T> scores);
	
	public double computeAverage(Iterable<List<T>> rankings) {
		double total = 0;
		int count = 0;
		
		for( List<T> ranking : rankings ) {
			total += compute(ranking);
			count++;
		}
		
		return total / count;
	}
	
	public static RankingMetric<Integer> getNumMistakesMetric() {
		
		return new RankingMetric<Integer>() {
			@Override
			public double compute(List<Integer> ranking) {				
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
			public double computeByScore(ScoredItems<Integer> scores) {
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
				
				Integer[] keys = scores.keySet().toArray(new Integer[scores.size()]);
				for( int i = 0; i < keys.length; i++ ) {
					for( int j = i + 1; j < keys.length; j++) {
						int compare = 
								new Double(
										scores.get(keys[i]).doubleValue()).compareTo(
												scores.get(keys[j]).doubleValue()); 
						if( compare < 0 )
							mistakes += 1;
						else if( compare == 0 )
							mistakes += 0.5;
					}
				}
				
				return mistakes;
			}			
		};
	}

	public static RankingMetric<Integer> getFirstPlaceWrongMetric() {		
		return new RankingMetric<Integer>() {
			@Override
			public double compute(List<Integer> ranking) {				
				List<Integer> sorted = new ArrayList<Integer>(ranking);
				Collections.sort(sorted);
								
				if( sorted.get(0) == ranking.get(0) ) return 0;				
				else return 1;
			}
			
			@Override
			public double computeByScore(ScoredItems<Integer> scores) {
				Integer[] keys = scores.keySet().toArray(new Integer[scores.size()]);
				
				double firstScore = scores.get(keys[0]).doubleValue();				
				int tiesForFirst = 1;
				
				for( int i = 1; i < keys.length; i++ ) {
					double otherScore = scores.get(keys[i]).doubleValue();
					if( firstScore < otherScore )
						return 1;
					else if( firstScore == otherScore )
						tiesForFirst++;
				}
				
				// How many other candidates could possibly win?
				return (tiesForFirst - 1.0) / tiesForFirst;				
			}			
		};
	}
	
	
}