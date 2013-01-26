package net.andrewmao.models.discretechoice;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.primitives.Ints;

import net.andrewmao.models.noise.NormalNoiseModel;
import net.andrewmao.probability.MultivariateNormal;
import net.andrewmao.probability.MultivariateNormal.ExpResult;
import net.andrewmao.socialchoice.rules.PreferenceProfile;
import net.andrewmao.stat.MultivariateMean;

/**
 * Numerical implementation of the ordered probit model, does not use MCMC
 * so it only supports a fixed variance.
 * 
 * @author mao
 *
 */
public class OrderedNormalEM extends RandomUtilityEstimator<NormalNoiseModel<?>> {	
	
	public static final double FIXED_VARIANCE = 1.0d;
	
	private final int maxIter;
	private final double abseps, releps;
	private final int maxMVNTries;
	
	public OrderedNormalEM(int maxIter, double abseps, double releps, int maxMVNTries) {
		this.maxIter = maxIter;
		this.abseps = abseps;
		this.releps = releps;
		this.maxMVNTries = maxMVNTries;
	}
	
	public OrderedNormalEM(int maxIter, double abseps, double releps) {
		this(maxIter, abseps, releps, 1);
	}

	@Override
	public double[] getParameters(List<int[]> rankings, int numItems) {
		int m = numItems;		
		
		RealVector mean = new ArrayRealVector(m, 0.0d);
//		RealVector mean = new ArrayRealVector(new NormalDistribution(0,1).sample(m), false);
		
		RealVector variance = new ArrayRealVector(m, FIXED_VARIANCE);
				
		MultivariateMean meanAccum = new MultivariateMean(m);
		double ll = Double.NEGATIVE_INFINITY;
		
		for(int i = 0; i < maxIter; i++ ) {
			// Yes, we HAVE to do this! Nasty bug ;) 
			meanAccum.clear();
			
			/* 
			 * E-step: compute conditional expectation
			 * only need to compute over unique rankings
			 */
			Multiset<List<Integer>> counts = HashMultiset.create();			
			for( int[] ranking : rankings )
				counts.add(Ints.asList(ranking));	
			
			double currentLL = 0;
			
			for( Entry<List<Integer>> e : counts.entrySet() ) {
				int[] ranking = Ints.toArray(e.getElement());	
				int duplicity = e.getCount();
				
				ExpResult result = multivariateExp(mean, variance, ranking, maxMVNTries, releps);				
				double[] condMean = computeConditional(result.values, ranking);				
				
				// Add this ll, expectation a number of times				
				for( int j = 0; j < duplicity; j++ ) {
					meanAccum.addValue(condMean);
				}				
				currentLL += duplicity * Math.log(result.cdf);
			}			
			
			// M-step: update mean
			double[] newMean = meanAccum.getMean();			
//			double adj = newMean[0];
			for( int j = 0; j < m; j++ )
				mean.setEntry(j, newMean[j]);
//				mean.setEntry(j, newMean[j] - adj);
			
			/*
			 * Check out how we did - log likelihood for the old mean is given for free above 
			 * almost 2x speedup over re-computing the LL from scratch			 
			 */					
			System.out.printf("Likelihood: %f\n", currentLL);
			double absImpr = currentLL - ll;
			double relImpr = -absImpr / ll;
			
			if( absImpr < abseps ) {
				System.out.printf("Absolute tolerance reached: %f < %f\n", absImpr, abseps);
				break;
			}
			if( relImpr < releps ) {
				System.out.printf("Relative tolerance reached: %f < %f\n", relImpr, releps);
				break;
			}
			
			ll = currentLL;
		}
		
		// Re-center means so first is 0
		double[] params = mean.toArray();
		double adj = params[0];
		for( int i = 0; i < params.length; i++ )
			params[i] -= adj;		
		return params;
		
//		return mean.toArray();
	}

	/**
	 * Compute the conditional expectation for this ranking using the multivariate normal expectation
	 * Equivalent to the MCMC Gibbs sampler with fixed mean 
	 * 
	 * @param mean
	 * @param variance
	 * @param ranking
	 * @return
	 */
	public static double[] conditionalExp(RealVector mean, RealVector variance, int[] ranking, int maxTries, double eps) {		
		double[] result = multivariateExp(mean, variance, ranking, maxTries, eps).values;				
		return computeConditional(result, ranking);
	}

	public static double[] computeConditional(double[] mvnexp, int[] ranking) {
		double[] vals = new double[mvnexp.length];
		
		// First value is the highest order statistic
		double str = mvnexp[0];
		vals[ranking[0]-1] = str;
		
		// Rest of values assigned by differences
		for( int i = 1; i < vals.length; i++ ) {
			vals[ranking[i]-1] = (str -= mvnexp[i]);
		}
		
		return vals;
	}

	public static ExpResult multivariateExp(RealVector mean, RealVector variance, int[] ranking, int maxTries, Double eps) {
		int n = ranking.length;
		
		// Initialize diagonal variance matrix
		RealMatrix d = new Array2DRowRealMatrix(n, n);
		for( int i = 0; i < n; i++ ) 
			d.setEntry(i, i, variance.getEntry(i));		
					
		/*
		 * Compute means and covariances of normal RVs
		 * of the highest value, then representing differences
		 * We want the expected value in the positive quadrant		
		 */
		RealMatrix a = new Array2DRowRealMatrix(n, n);
		double[] lower = new double[n];
		double[] upper = new double[n];
		
		// Expected value of highest strength (top in ranking)
		a.setEntry(0, ranking[0]-1, 1.0);
		lower[0] = Double.NEGATIVE_INFINITY;
		upper[0] = Double.POSITIVE_INFINITY;
		
		// Expected values of differences
		for( int i = 1; i < n; i++ ) {
			a.setEntry(i, ranking[i-1]-1, 1.0);
			a.setEntry(i, ranking[i]-1, -1.0);
			lower[i] = 0.0d; // already initialized by default...
			upper[i] = Double.POSITIVE_INFINITY;
		}
				
		RealVector mu = a.transpose().preMultiply(mean);
		RealMatrix sigma = a.multiply(d).multiply(a.transpose());		
		
		return MultivariateNormal.exp(mu, sigma, lower, upper, maxTries, eps, eps);		
	}

	@Override
	public <T> NormalNoiseModel<T> fitModel(PreferenceProfile<T> profile) {
		List<T> ordering = Arrays.asList(profile.getSortedCandidates());
		List<int[]> rankings = profile.getIndices(ordering);		
		
		int m = ordering.size();
		double[] strParams = getParameters(rankings, m);		
		
		return new NormalNoiseModel<T>(ordering, new Random(), strParams, Math.sqrt(FIXED_VARIANCE));		
	}

}
