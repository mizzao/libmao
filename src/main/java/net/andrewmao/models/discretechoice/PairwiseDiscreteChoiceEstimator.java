package net.andrewmao.models.discretechoice;

import java.util.List;

import net.andrewmao.models.noise.NoiseModel;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

public abstract class PairwiseDiscreteChoiceEstimator<M extends NoiseModel<?>> extends DiscreteChoiceEstimator<M> {		
	
	protected <T> double[][] addAdjacentPairs(PreferenceProfile<T> profile, List<T> ordering) {
		int m = ordering.size();		
		double[][] wins = new double[m][m];
		
		for( T[] ranking : profile.getProfile() ) {
			for( int i = 0; i < ranking.length - 1; i++ ) {
				int idxWinner = ordering.indexOf(ranking[i]);
				int idxLoser = ordering.indexOf(ranking[i+1]);
				
				wins[idxWinner][idxLoser] += 1;
			}
		}
		
		return wins;
	}
	
	protected <T> double[][] addAllPairs(PreferenceProfile<T> profile, List<T> ordering) {		
		int m = ordering.size();		
		double[][] wins = new double[m][m];
		
		for( T[] ranking : profile.getProfile() ) {
			for( int i = 0; i < ranking.length; i++ ) {
				for( int j = i+1; j < ranking.length; j++ ) {
					int idxWinner = ordering.indexOf(ranking[i]);
					int idxLoser = ordering.indexOf(ranking[j]);
					
					wins[idxWinner][idxLoser] += 1;					
				}
			}
		}
		
		return wins;
	}
	
	@Override
	public <T> M fitModel(PreferenceProfile<T> profile) {
		return this.fitModel(profile, true);
	}

	public abstract <T> M fitModel(PreferenceProfile<T> profile, boolean useAllPairs);
	
	public abstract double[] getParameters(double[][] winMatrix);
}
