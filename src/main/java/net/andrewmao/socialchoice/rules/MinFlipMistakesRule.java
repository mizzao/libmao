package net.andrewmao.socialchoice.rules;

import java.util.LinkedList;
import java.util.List;

import ilog.concert.IloConstraint;
import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjectiveSense;

public abstract class MinFlipMistakesRule<T> extends OptimizedPositionalRule<T> {					
	
	IloIntExpr[] flipArr;
	
	@Override
	public void optimize(PreferenceProfileList<T> preferences, double normalization) throws 
	IloException {	
		super.optimize(preferences, normalization);		
		
		// List of sum of flips and list of flip vars for each							
		flipArr = new IloIntExpr[preferences.size()];
		
		int p = 0;
		for( PreferenceProfile<T> profile : preferences ) {				
			IloLinearNumExpr[] totals = profileTotals.get(p);
						
			T[] choices = profile.getSortedCandidates();		
			
			initStep(p, profile);									
			
			List<IloIntVar> flips = new LinkedList<IloIntVar>();
			for( int i = 0; i < choices.length; i++ ) {
				for( int j = i + 1; j < choices.length; j++ ) {
					// score of i should be greater than score if j, if not then we have a flip
					IloConstraint mistake = cp.le(totals[i], totals[j]);
//					IloConstraint mistake = cp.ge(totals[j], totals[i]);
					
					IloIntVar flip = cp.boolVar(choices[i] + ">" + choices[j]);
					IloConstraint flipGe1 = cp.ge(flip, 1);
					cp.add(cp.ifThen(mistake, flipGe1));
					
					flips.add(flip);
					
					processPair(p, profile, flip, i, j, totals[i], totals[j]);
				}
			}
			
			flipArr[p] = cp.sum((IloIntVar[]) flips.toArray(new IloIntVar[super.getNumPairs()]));
			
			/* Normalize so that the sum of the minMargins can be no greater than 1
			 * so that the flips always dominate
			 */
			
			completeStep(p);
			
			p++;
		}			
	}
	
	protected void initStep(int p, PreferenceProfile<T> profile) throws IloException {}

	protected void processPair(int p, PreferenceProfile<T> profile, 
			IloIntVar flip, int i, int j, IloLinearNumExpr total_i, IloLinearNumExpr total_j) throws IloException {}

	protected void completeStep(int p) throws IloException {}

	protected void solve(IloNumExpr subObj) throws IloException {
		cp.addObjective(IloObjectiveSense.Minimize, subObj != null ?
				cp.sum(cp.sum(flipArr), subObj) : cp.sum(flipArr) );
		
//		int i = 3;
//		for( IloNumVar score : super.posScores ) {
//			score.setUB(i);
//			score.setLB(i);
//			i--;
//		}
		
		cp.solve();
		
		scores = cp.getValues(posScores);
//		System.out.print(this + ": ");
//		for( double d : scores)
//			System.out.printf("%.04f ", d);
//		System.out.println();
		
//		System.out.println("Objective: " + cp.getObjValue());		
//		System.out.println("Min mistakes: " + cp.getValue(cp.sum(flipArr)));
		
//		int mistakes = 0;		
//		double[][] stuff = super.getTotalScores();
//		for( int i = 0; i < stuff.length; i++ ) {
//			System.out.print(cp.getValue(flipArr[i]) + " ");
//			mistakes += PositionalVotingRule.prettyPrint(stuff[i]);			
//		}
//		System.out.println("Counted " + mistakes + " mistakes.");
			
//		System.out.println("Min margin: " + cp.getValue(minMargin));
//		System.out.println("Min margin norm: " + cp.getValue(minMargin) / profile.length );
//		
//		for( IloIntVar flip : flipArr ) System.out.println(flip.toString() + ": " + cp.getValue(flip));			
	}
	
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
	public static class Vanilla<T> extends MinFlipMistakesRule<T> {		
		@Override
		public void optimize(PreferenceProfileList<T> preferences, double normalization)
				throws IloException {	
			super.optimize(preferences, normalization);
			super.solve(null);
		}		
	}
	
	public static class MaxSumMinEachMargin<T> extends MinFlipMistakesRule<T> {
		
		IloNumVar[] marginMinsNorm;
		
		int k;
		double currentMaxPossibleMargin;
		
		@Override
		public void optimize(PreferenceProfileList<T> preferences, double normalization)
				throws IloException {
			// TODO remove this hardcoded number
			marginMinsNorm = cp.numVarArray(6, Double.NEGATIVE_INFINITY, 0);
			
			super.optimize(preferences, normalization);					
			
			super.solve(cp.sum(marginMinsNorm));
		}

		@Override
		protected void initStep(int p, PreferenceProfile<T> profile) throws IloException {
			k = 0;
			currentMaxPossibleMargin = normalization * profile.getNumRankings();
		}

		@Override
		protected void processPair(int p, PreferenceProfile<T> profile, IloIntVar flip, int i,
				int j, IloLinearNumExpr total_i, IloLinearNumExpr total_j)
				throws IloException {
			IloNumExpr condConstr = 
					cp.sum( cp.prod(flip, currentMaxPossibleMargin), cp.diff(total_i, total_j));							
				
			cp.addGe(marginMinsNorm[k],						
					cp.prod(-1.0 / currentMaxPossibleMargin / preferences.size(), condConstr));
			
			k++;
		}
		
	}
	
	public static class MaxMinFirstLastMargin<T> extends MinFlipMistakesRule<T> {
		IloNumVar overallMinMargin;
		double currentMaxPossibleMinMargin;
		
		@Override
		public void optimize(PreferenceProfileList<T> preferences, double normalization)
				throws IloException {
			overallMinMargin = cp.numVar(Double.NEGATIVE_INFINITY, 0);
			super.optimize(preferences, normalization);
			super.solve(overallMinMargin);
		}

		@Override
		protected void initStep(int p, PreferenceProfile<T> profile) throws IloException {			
			currentMaxPossibleMinMargin = normalization / (numCandidates - 1) * profile.getNumRankings();						
		}
		
		@Override
		protected void processPair(int p, PreferenceProfile<T> profile,
				IloIntVar flip, int i, int j, IloLinearNumExpr total_i, IloLinearNumExpr total_j)
				throws IloException {
			if( i == 0 && j == numCandidates - 1) {
				IloNumVar currentMinMargin = cp.numVar(0, Double.POSITIVE_INFINITY);
				IloNumExpr condConstr = 
						cp.sum( cp.prod(flip, currentMaxPossibleMinMargin), cp.diff(total_i, total_j));				
				cp.addLe(currentMinMargin, condConstr);	
					
				cp.addGe(overallMinMargin,						
						cp.prod(-1.0 / currentMaxPossibleMinMargin / preferences.size(), currentMinMargin));
			}			
		}	
	}
	
	public static class MaxOverallMinMargin<T> extends MinFlipMistakesRule<T> {		
		IloNumVar overallMinMargin;

		IloNumVar currentMinMargin;
		double currentMaxPossibleMinMargin;

		@Override
		public void optimize(PreferenceProfileList<T> preferences, double normalization)
		throws IloException {				
			// Using the negative of this value
			overallMinMargin = cp.numVar(Double.NEGATIVE_INFINITY, 0);
			super.optimize(preferences, normalization);
			super.solve(overallMinMargin);
		}

		@Override
		protected void initStep(int p, PreferenceProfile<T> profile) throws IloException {
			// TODO check that we can actually divide this by (c-1)
			currentMaxPossibleMinMargin = normalization / (numCandidates - 1) * profile.getNumRankings();
			currentMinMargin = cp.numVar(0, Double.POSITIVE_INFINITY);
		}

		@Override
		protected void processPair(int p, PreferenceProfile<T> profile,
				IloIntVar flip, int i, int j, IloLinearNumExpr total_i, IloLinearNumExpr total_j) throws IloException {
			// Add margin for non-flipped (not just adjacent) pairs
			IloNumExpr margin = cp.diff(total_i, total_j);
			IloNumExpr condConstr = 
				cp.sum( cp.prod(flip, currentMaxPossibleMinMargin), margin);
			cp.addLe(currentMinMargin, condConstr);				
		}

		@Override
		protected void completeStep(int p) throws IloException {
			cp.addGe(overallMinMargin,						
					cp.prod(-1.0 / currentMaxPossibleMinMargin / preferences.size(), currentMinMargin));
		}			

	}
	
	public static class MaxSumMinMargin<T> extends MinFlipMistakesRule<T> {
		double currentMaxPossibleMinMargin;
		IloNumVar currentMinMargin;		

		IloNumExpr[] minMarginsNorm;

		@Override
		public void optimize(PreferenceProfileList<T> preferences, double normalization)
		throws IloException {								
			minMarginsNorm = new IloNumExpr[preferences.size()];
			super.optimize(preferences, normalization);
	
			super.solve(cp.sum(minMarginsNorm));				
		}

		@Override
		protected void initStep(int p, PreferenceProfile<T> profile) throws IloException {
			currentMaxPossibleMinMargin = normalization / (numCandidates - 1) * profile.getNumRankings();
			currentMinMargin = cp.numVar(0, Double.POSITIVE_INFINITY);	
		}

		@Override
		protected void processPair(int p, PreferenceProfile<T> profile,
				IloIntVar flip, int i, int j, IloLinearNumExpr total_i, IloLinearNumExpr total_j) throws IloException {
			IloNumExpr margin = cp.diff(total_i, total_j);
			IloNumExpr condConstr = 
				cp.sum( cp.prod(flip, currentMaxPossibleMinMargin), margin);
			cp.addLe(currentMinMargin, condConstr);
		}

		@Override
		protected void completeStep(int p) throws IloException {			
			minMarginsNorm[p] =	cp.prod(
					currentMinMargin, 
					-1.0 / currentMaxPossibleMinMargin / preferences.size() 
					);
		}			
	}
}
