package net.andrewmao.models.discretechoice;

import java.util.List;

public abstract class PairwiseDiscreteChoiceEstimator<T> extends DiscreteChoiceEstimator<T> {

	protected PairwiseDiscreteChoiceEstimator(List<T> items) {
		super(items);		
	}
	
	public abstract void addData(T winner, T loser, int count);
	
	public void addAdjacentPairs(T[] ranking) {
		for( int i = 0; i < ranking.length - 1; i++ ) {				
			addData(ranking[i], ranking[i+1], 1);				
		}
	}
	
	public void addAllPairs(T[] ranking) {		
		for( int i = 0; i < ranking.length; i++ ) {
			for( int j = i+1; j < ranking.length; j++ ) {
				addData(ranking[i], ranking[j], 1);
			}
		}
	}

}
