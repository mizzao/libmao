package net.andrewmao.models.discretechoice;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import net.andrewmao.models.noise.NormalLogLikelihood;
import net.andrewmao.models.noise.NormalNoiseModel;
import net.andrewmao.socialchoice.rules.PreferenceProfile;
import net.andrewmao.stat.MultivariateMean;

/**
 * Numerical implementation of the probit model, does not use MCMC
 * so it only supports a fixed variance.
 * 
 * @author mao
 *
 */
public class OrderedNormalEM extends RandomUtilityEstimator<NormalNoiseModel<?>> {

	private final int maxIter;
	private final double abseps, releps;
	
	public OrderedNormalEM(int maxIter, double abseps, double releps) {
		this.maxIter = maxIter;
		this.abseps = abseps;
		this.releps = releps;
	}
	
	@Override
	public double[] getParameters(List<int[]> rankings, int numItems) {
		int m = numItems;		
		
		RealVector mean = new ArrayRealVector(m, 0.0d);
		RealVector variance = new ArrayRealVector(m, 1.0d);
		
		NormalLogLikelihood logLk = new NormalLogLikelihood(mean, variance);
		MultivariateMean meanAccum = new MultivariateMean(m);
		double ll = Double.NEGATIVE_INFINITY;
		
		for(int i = 0; i < maxIter; i++ ) {
			// E-step: compute conditional expectation
			for( int[] ranking : rankings ) {
				meanAccum.addValue(computeExpectation(mean, variance, ranking));
			}
			
			// M-step: update mean
			double[] newMean = meanAccum.getMean();
			for( int j = 0; j < m; j++ )
				mean.setEntry(j, newMean[j]);
			
			// Check out how we did
			double newLL = logLk.logLikelihood(rankings);			
			System.out.printf("Likelihood: %f\n", newLL);
			double absImpr = newLL - ll;
			double relImpr = -absImpr / ll;
			
			if( absImpr < abseps ) {
				System.out.printf("Absolute tolerance reached: %f < %f\n", absImpr, abseps);
				break;
			}
			if( relImpr < releps ) {
				System.out.printf("Relative tolerance reached: %f < %f\n", relImpr, releps);
				break;
			}
			
			ll = newLL;
		}
				
		return mean.toArray();
	}

	/**
	 * Compute the conditional expectation for this ranking using the multivariate normal expectation 
	 * 
	 * @param mean
	 * @param variance
	 * @param ranking
	 * @return
	 */
	public static double[] computeExpectation(RealVector mean, RealVector variance, int[] ranking) {
		
		return null;
	}

	@Override
	public <T> NormalNoiseModel<T> fitModel(PreferenceProfile<T> profile) {
		List<T> ordering = Arrays.asList(profile.getSortedCandidates());
		List<int[]> rankings = profile.getIndices(ordering);		
		
		double[] strParams = getParameters(rankings, ordering.size());
		
		return new NormalNoiseModel<T>(ordering, new Random(), strParams);		
	}

}
