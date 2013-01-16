package net.andrewmao.models.discretechoice;

import java.util.List;

import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.OptimizationData;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer.Formula;

public class BradleyTerryModel<T> extends PairwiseDiscreteChoiceModel<T> {

	int[][] wins;
	
	NonLinearConjugateGradientOptimizer optim, backup;	
	
	public BradleyTerryModel(List<T> items) {
		super(items);
		
		ConvergenceChecker<PointValuePair> checker = 
				new SimplePointChecker<PointValuePair>(1e-5, 1e-8);
		
		optim = new NonLinearConjugateGradientOptimizer(Formula.POLAK_RIBIERE, checker);
		backup = new NonLinearConjugateGradientOptimizer(Formula.FLETCHER_REEVES, checker);
		
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
		BTNLogLikelihood nll = new BTNLogLikelihood(wins);
		
		OptimizationData func = new ObjectiveFunction(nll);
		OptimizationData grad = new ObjectiveFunctionGradient(nll.gradient());		
		OptimizationData start = new InitialGuess(new double[items.size() - 1]);
				
		PointValuePair result = null;
		// use Polak-Ribiere unless fails to converge; then switch to Fletcher-Reeves
		try {
			OptimizationData maxIter = new MaxIter(100);
			result = optim.optimize(func, grad, GoalType.MINIMIZE, start, maxIter);
		}
		catch( MathIllegalStateException e ) {
			OptimizationData maxEval = new MaxEval(500);
			result = backup.optimize(func, grad, GoalType.MINIMIZE, start, maxEval);
		}				
		
		RealVector strEst = new ArrayRealVector(new double[] {0.0}, result.getPoint());
		
		return new ScoredItems<T>(items, strEst.toArray());
	}

}
