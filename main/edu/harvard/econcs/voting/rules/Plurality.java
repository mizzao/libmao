package edu.harvard.econcs.voting.rules;

public class Plurality extends PositionalVotingRule {

	double d;
	
	public Plurality(double i) {
		d = i;
	}

	@Override
	protected double[] getPositionalScores(int length) {
		double[] scores = new double[length];
		
		scores[0] = d;
		
		return scores;
	}

}
