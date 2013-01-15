package net.andrewmao.models.discretechoice;

import java.util.List;

import org.apache.commons.math3.analysis.DifferentiableMultivariateFunction;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.general.ConjugateGradientFormula;
import org.apache.commons.math3.optimization.general.NonLinearConjugateGradientOptimizer;

public class BradleyTerryModel<T> extends PairwiseDiscreteChoiceModel<T> {

	int[][] wins;
	
	NonLinearConjugateGradientOptimizer optim, backup;	
	
	public BradleyTerryModel(List<T> items) {
		super(items);
		
		optim = new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE);
		backup = new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.FLETCHER_REEVES);
		
		wins = new int[items.size()][items.size()];
	}

	@Override
	public void addData(T winner, T loser, int count) {
		int idxWinner = items.indexOf(winner);
		int idxLoser = items.indexOf(loser);
		
		wins[idxWinner][idxLoser] += count;
	}

	@Override
	public ScoredItems<T> getParameters() {
		double[] start = new double[items.size() - 1];
		DifferentiableMultivariateFunction nll = new BTNLogLikelihood(wins);
				
		PointValuePair result = null;
		// use Polak-Ribiere unless fails to converge; then switch to Fletcher-Reeves
		try {
			result = optim.optimize(100, nll, GoalType.MINIMIZE, start);
		}
		catch( MathIllegalStateException e ) {
			result = backup.optimize(500, nll, GoalType.MINIMIZE, start);
		}				
		
		RealVector strEst = new ArrayRealVector(new double[] {0.0}, result.getPoint());
		
		return new ScoredItems<T>(items, strEst.toArray());
	}

}
