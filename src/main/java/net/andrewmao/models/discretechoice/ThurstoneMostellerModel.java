package net.andrewmao.models.discretechoice;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math3.linear.RealMatrix;
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

public class ThurstoneMostellerModel<T> extends PairwiseDiscreteChoiceEstimator<T> {
	
	public static final AtomicInteger cgUses = new AtomicInteger();
	public static final AtomicInteger powellUses = new AtomicInteger();
	
	private RealMatrix mat;	
	private TMNLogLikelihood nll;
	
	final boolean useGradient;
		
	NonLinearConjugateGradientOptimizer optim;
	PowellOptimizer backup;
	
	public ThurstoneMostellerModel(List<T> items, boolean useGradient) {
		super(items);		
		this.useGradient = useGradient;
		
		mat = new Array2DRowRealMatrix(items.size(), items.size());		
		mat.walkInRowOrder(new DefaultRealMatrixChangingVisitor() {
			public double visit(int row, int column, double value) {
				if (row == column)
					return 0;
				else
					return 0.08;
			}
		});
				
		nll = new TMNLogLikelihood(mat);
		
		ConvergenceChecker<PointValuePair> checker = 
				new SimplePointChecker<PointValuePair>(1e-5, 1e-8);
		optim = new NonLinearConjugateGradientOptimizer(Formula.POLAK_RIBIERE, checker);

		// TODO: fix gradient optimizer (why is it broken ~20% of the time?)
		backup = new PowellOptimizer(1e-7, 1e-11); // From commons 2.2 defaults
	}
	
	public ThurstoneMostellerModel(List<T> items) {
		this(items, true);
	}
	
	@Override
	public void addData(T winner, T loser, int count) {
		int winIdx = items.indexOf(winner);
		int loseIdx = items.indexOf(loser);
		
		mat.setEntry(winIdx, loseIdx, mat.getEntry(winIdx, loseIdx) + count);		
	}

	@Override
	public ScoredItems<T> getParameters() {
		double[] params = null;
		if( useGradient ) {
			try {
				params = gradientParameters();
				cgUses.incrementAndGet();			
			} catch(MathIllegalStateException e ) {}	
		}		
		if( params == null ) {
			params = powellParameters();
			powellUses.incrementAndGet();
		}
		
		RealVector strEst = new ArrayRealVector(new double[] {0.0}, params);
				
		return new ScoredItems<T>(items, strEst.toArray());
	}

	private double[] gradientParameters() {
		OptimizationData func = new ObjectiveFunction(nll);
		OptimizationData grad = new ObjectiveFunctionGradient(nll.gradient());		
		OptimizationData start = new InitialGuess(new double[items.size() - 1]);
		
		PointValuePair result = optim.optimize(func, grad, GoalType.MINIMIZE, start, 
				new MaxIter(500), new MaxEval(500));
		
		return result.getPointRef();
	}

	private double[] powellParameters() {
		OptimizationData func = new ObjectiveFunction(nll);
		OptimizationData grad = new ObjectiveFunctionGradient(nll.gradient());		
		OptimizationData start = new InitialGuess(new double[items.size() - 1]);		
		
		PointValuePair result = backup.optimize(func, grad, GoalType.MINIMIZE, start,
				new MaxEval(5000), new MaxIter(1000));
		
		return result.getPointRef();
	}

}
