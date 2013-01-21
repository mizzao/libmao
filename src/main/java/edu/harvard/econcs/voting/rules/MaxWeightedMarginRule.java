package edu.harvard.econcs.voting.rules;

import java.util.Arrays;
import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloObjectiveSense;

public class MaxWeightedMarginRule<T> extends OptimizedPositionalRule<T> {
	
	@Override
	public void optimize(PreferenceProfileList<T> preferences, double normalization)
			throws IloException {		
		super.optimize(preferences, normalization);
		
		int adjacentSpaces = numCandidates - 1;

		IloNumExpr[] diffs = new IloIntExpr[preferences.size()];
		
		for( int p = 0; p < preferences.size(); p++ ) {	
			IloNumExpr[] totals = super.profileTotals.get(p);
			IloNumExpr[] subDiffs = new IloNumExpr[adjacentSpaces];

			for( int i = 0; i < adjacentSpaces; i++ )
				subDiffs[i] = cp.prod(adjacentSpaces - i, cp.diff(totals[i], totals[i+1]));
			
			diffs[p] = cp.sum(subDiffs);
		}		
		
		cp.addObjective(IloObjectiveSense.Maximize, cp.sum(diffs));
		
		cp.solve();
		
		scores = cp.getValues(posScores);
		System.out.println(this.getClass().getSimpleName() + ": " + Arrays.toString(scores));
	}

}
