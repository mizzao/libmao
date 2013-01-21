package net.andrewmao.socialchoice.rules;

import java.util.Arrays;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjectiveSense;

public class MaximinMarginRule<T> extends OptimizedPositionalRule<T> {
	
	@Override
	public void optimize(PreferenceProfileList<T> preferences, double normalization)
			throws IloException {		
		super.optimize(preferences, normalization);
		
		/* TODO this currently is broken if the preference profile has no way
		 *  of being consistent with the ground truth. See the implementation in min mistakes.
		 */
		
		IloNumVar[] minMargins = new IloNumVar[preferences.size()];
				
		for( int p = 0; p < preferences.size(); p++ ) {
			IloLinearNumExpr[] totals = super.profileTotals.get(p);
			
			IloNumVar min = cp.numVar(0, Double.POSITIVE_INFINITY); 
			for( int i = 0; i < numCandidates - 1; i++ ) {
				cp.addLe(min, cp.diff(totals[i], totals[i+1]));
			}
			
			minMargins[p] = min;
		}		
		
		cp.addObjective(IloObjectiveSense.Maximize, cp.sum(minMargins));
		
		cp.solve();
		
		scores = cp.getValues(posScores);
		System.out.println(this.getClass().getSimpleName() + ": " + Arrays.toString(scores));
	}

}
