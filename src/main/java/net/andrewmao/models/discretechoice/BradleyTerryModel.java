package net.andrewmao.models.discretechoice;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizer;

public class BradleyTerryModel<T> extends PairwiseDiscreteChoiceModel<T> {

	public static final AtomicInteger cgUses = new AtomicInteger();
	public static final AtomicInteger powellUses = new AtomicInteger();
	
	int[][] wins;
	
	NonLinearConjugateGradientOptimizer optim;
	PowellOptimizer backup;	
	
	public BradleyTerryModel(List<T> items) {
		super(items);
		
		ConvergenceChecker<PointValuePair> checker = 
				new SimplePointChecker<PointValuePair>(1e-5, 1e-8);
		
		optim = new NonLinearConjugateGradientOptimizer(Formula.POLAK_RIBIERE, checker);
//		backup = new NonLinearConjugateGradientOptimizer(Formula.FLETCHER_REEVES, checker);
		backup = new PowellOptimizer(1e-5, 1e-8);
		
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
		// use Polak-Ribiere unless fails to converge
		try {			
			result = optim.optimize(func, grad, GoalType.MINIMIZE, start, 
					new MaxIter(500), new MaxEval(500));
			cgUses.incrementAndGet();
		}
		catch( MathIllegalStateException e ) {
			// TODO: Default value of 50 iterations in BracketFinder here seems to cause problems
			result = backup.optimize(func, GoalType.MINIMIZE, start, 
					new MaxEval(5000), new MaxIter(1000));
			powellUses.incrementAndGet();
		}
		
		RealVector strEst = new ArrayRealVector(new double[] {0.0}, result.getPoint());
		
		return new ScoredItems<T>(items, strEst.toArray());
	}

}
