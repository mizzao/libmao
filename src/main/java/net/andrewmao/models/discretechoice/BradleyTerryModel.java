package net.andrewmao.models.discretechoice;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import net.andrewmao.models.noise.GumbelNoiseModel;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

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

public class BradleyTerryModel extends PairwiseDiscreteChoiceEstimator<GumbelNoiseModel<?>> {

	public static final AtomicInteger cgUses = new AtomicInteger();
	public static final AtomicInteger powellUses = new AtomicInteger();
			
	NonLinearConjugateGradientOptimizer optim;
	PowellOptimizer backup;	
	
	public BradleyTerryModel() {		
		ConvergenceChecker<PointValuePair> checker = 
				new SimplePointChecker<PointValuePair>(1e-5, 1e-8);
		
		optim = new NonLinearConjugateGradientOptimizer(Formula.POLAK_RIBIERE, checker);
//		backup = new NonLinearConjugateGradientOptimizer(Formula.FLETCHER_REEVES, checker);
		backup = new PowellOptimizer(1e-5, 1e-8);
				
	}
	
	@Override
	public double[] getParameters(double[][] wins) {				
		BTNLogLikelihood nll = new BTNLogLikelihood(wins);
		
		OptimizationData func = new ObjectiveFunction(nll);
		OptimizationData grad = new ObjectiveFunctionGradient(nll.gradient());		
		OptimizationData init = new InitialGuess(new double[wins.length - 1]);
				
		PointValuePair result = null;
		// use Polak-Ribiere unless fails to converge
		try {			
			result = optim.optimize(func, grad, GoalType.MINIMIZE, init, 
					new MaxIter(500), new MaxEval(500));
			cgUses.incrementAndGet();
		}
		catch( MathIllegalStateException e ) {
			/* Had problems with returning 0.
			 * Initialize with some small random numbers...
			 */
			double[] start = new double[wins.length - 1];
			for( int i = 0; i < start.length; i++ )
				start[i] = 0.1 * Math.random() - 0.5;
			init = new InitialGuess(start);
			
			// TODO: Default value of 50 iterations in BracketFinder here seems to cause problems
			result = backup.optimize(func, GoalType.MINIMIZE, init, 
					new MaxEval(5000), new MaxIter(1000));
			powellUses.incrementAndGet();
		}
		
		RealVector strEst = new ArrayRealVector(new double[] {0.0}, result.getPoint());		
		return strEst.toArray();
	}

	@Override
	public <T> GumbelNoiseModel<T> fitModel(PreferenceProfile<T> profile, boolean useAllPairs) {
		List<T> ordering = Arrays.asList(profile.getSortedCandidates());
		
		double[][] wins = useAllPairs ? 
				super.addAllPairs(profile, ordering) : 
					super.addAdjacentPairs(profile, ordering);
		
		double[] strParams = getParameters(wins);
		
		return new GumbelNoiseModel<T>(ordering, new Random(), strParams);
	}

}
