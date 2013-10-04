package net.andrewmao.models.discretechoice;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.andrewmao.models.noise.NormalNoiseModel;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

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

public class ThurstoneMostellerModel extends PairwiseDiscreteChoiceEstimator<NormalNoiseModel<?>> {
	
	public static final double THURSTONE_SIGMA = Math.sqrt(0.5);
	
	public static final AtomicInteger cgUses = new AtomicInteger();
	public static final AtomicInteger powellUses = new AtomicInteger();		
	
	final boolean useGradient;
		
	NonLinearConjugateGradientOptimizer optim;
	PowellOptimizer backup;
	
	public ThurstoneMostellerModel(boolean useGradient) {			
		this.useGradient = useGradient;		
		
		ConvergenceChecker<PointValuePair> checker = 
				new SimplePointChecker<PointValuePair>(1e-5, 1e-8);
		optim = new NonLinearConjugateGradientOptimizer(Formula.POLAK_RIBIERE, checker);

		// TODO: fix gradient optimizer (why is it broken ~20% of the time?)
		backup = new PowellOptimizer(1e-7, 1e-11); // From commons 2.2 defaults
	}
	
	public ThurstoneMostellerModel() {
		this(true);
	}

	@Override
	public double[] getParameters(double[][] wins) {
		RealMatrix winMat = new Array2DRowRealMatrix(wins, false);
		
		// Thomas' original perturbation to make sure the matrix is positive or whatever
		winMat.walkInRowOrder(new DefaultRealMatrixChangingVisitor() {
			public double visit(int row, int column, double value) {
				if (row == column)
					return value;
				else
					return value + 0.08;
			}
		});		
		
		TMNLogLikelihood nll = new TMNLogLikelihood(winMat);
		
		double[] params = null;
		if( useGradient ) {
			try {
				params = gradientParameters(nll);
				cgUses.incrementAndGet();			
			} catch(MathIllegalStateException e ) {}	
		}		
		if( params == null ) {
			params = powellParameters(nll);
			powellUses.incrementAndGet();
		}
		
		RealVector strEst = new ArrayRealVector(new double[] {0.0}, params);
		return strEst.toArray();
	}

	private double[] gradientParameters(TMNLogLikelihood nll) {
		OptimizationData func = new ObjectiveFunction(nll);
		OptimizationData grad = new ObjectiveFunctionGradient(nll.gradient());		
		OptimizationData start = new InitialGuess(new double[nll.mat.getRowDimension() - 1]);
		
		PointValuePair result = optim.optimize(func, grad, GoalType.MINIMIZE, start, 
				new MaxIter(500), new MaxEval(500));
		
		return result.getPointRef();
	}

	private double[] powellParameters(TMNLogLikelihood nll) {
		// Random perturbations seems to help Powell along a bit
		double[] start = new double[nll.mat.getRowDimension() - 1];
		for( int i = 0; i < start.length; i++ )
			start[i] = 0.1 * Math.random() - 0.5;
		
		OptimizationData func = new ObjectiveFunction(nll);			
		OptimizationData init = new InitialGuess(start);		
		
		PointValuePair result = backup.optimize(func, GoalType.MINIMIZE, init,
				new MaxEval(5000), new MaxIter(1000));
		
		return result.getPointRef();
	}

	@Override
	public <T> NormalNoiseModel<T> fitModel(PreferenceProfile<T> profile, boolean useAllPairs) {
		List<T> ordering = Arrays.asList(profile.getSortedCandidates());
		
		double[][] wins = useAllPairs ? 
				super.addAllPairs(profile, ordering) : 
					super.addAdjacentPairs(profile, ordering);
		
		double[] strParams = getParameters(wins);
		
		return new NormalNoiseModel<T>(ordering, strParams, THURSTONE_SIGMA);		
	}

}
