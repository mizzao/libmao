package net.andrewmao.socialchoice.rules;

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
	
}