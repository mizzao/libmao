package edu.harvard.econcs.voting.rules;

public class Borda extends PositionalVotingRule {
		
	@Override
	protected double[] getPositionalScores(int length) {		
		double[] scores = new double[length];
		
		for( int i = 0; i < length; i++ ) scores[i] = length - i - 1;
		
		return scores;
	}

}
