package net.andrewmao.socialchoice.rules;

import java.util.Arrays;
import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloObjectiveSense;

public class MaxSumMarginRule<T> extends OptimizedPositionalRule<T> {
	
	@Override
	public void optimize(PreferenceProfileList<T> preferences, double normalization)
			throws IloException {		
		super.optimize(preferences, normalization);
		
		// TODO broken if the prefs from first to last are inconsistent
		
		IloNumExpr[] diffs = new IloIntExpr[preferences.size()];
		
		for( int p = 0; p < preferences.size(); p++ ) {			
			IloLinearNumExpr[] totals = super.profileTotals.get(p);
			diffs[p] = cp.diff(totals[0], totals[totals.length - 1]);				
		}
		
		cp.addObjective(IloObjectiveSense.Maximize, cp.sum(diffs));		
		cp.solve();
		
		scores = cp.getValues(posScores);
		System.out.println(this.getClass().getSimpleName() + ": " + Arrays.toString(scores));
	}

}
