package net.andrewmao.models.discretechoice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.math3.analysis.function.Sqrt;
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
import net.andrewmao.probability.MultivariateNormal.EX2Result;
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
	
	public static final int EM_MAXPTS_MULTIPLIER = 1 << 14;
	
	volatile double lastLL;
	volatile RealVector lastVariance;
	
	private final boolean floatVariance;
	private final int maxIter;
	private final double abseps, releps;
	private final int maxPtsScale;
	
	public OrderedNormalEM(boolean floatVariance, int maxIter, double abseps, double releps, int maxPtsScale) {
		this.floatVariance = floatVariance;
		this.maxIter = maxIter;
		this.abseps = abseps;
		this.releps = releps;
		this.maxPtsScale = maxPtsScale;
	}
	
	public OrderedNormalEM(boolean floatVariance, int maxIter, double abseps, double releps) {
		this(floatVariance, maxIter, abseps, releps, EM_MAXPTS_MULTIPLIER);
	}

	@Override
	public double[] getParameters(List<int[]> rankings, int numItems) {
		int m = numItems;		
		
		final RealVector mean = new ArrayRealVector(m, 0.0d);
//		RealVector mean = new ArrayRealVector(new NormalDistribution(0,1).sample(m), false);
		
		final RealVector variance = new ArrayRealVector(m, FIXED_VARIANCE);
				
		MultivariateMean m1Stats = new MultivariateMean(m), m2Stats = null;
		if( floatVariance ) m2Stats = new MultivariateMean(m);
		double ll = Double.NEGATIVE_INFINITY;
		
		// Pre-compute a single hash of rankings
		Multiset<List<Integer>> counts = HashMultiset.create();			
		for( int[] ranking : rankings )
			counts.add(Ints.asList(ranking));
		
		for(int i = 0; i < maxIter; i++ ) {
			// Need to empty out the previous iteration's means. Nasty bug ;) 
			m1Stats.clear();
			if( floatVariance ) m2Stats.clear();
			
			System.out.println("Starting iteration " + i);
			
			/* 
			 * E-step: compute conditional expectation
			 * only need to compute over unique rankings
			 */				
			double currentLL = 0;						
			
			// TODO: abstract this silly control logic
			if( floatVariance ) {				
				List<Callable<M2Result>> tasks = new ArrayList<Callable<M2Result>>(counts.entrySet().size());	
				for( Entry<List<Integer>> e : counts.entrySet() ) {
					final int[] ranking = Ints.toArray(e.getElement());	
					final int duplicity = e.getCount();								
																	
					tasks.add(new Callable<M2Result>() {
						@Override
						public M2Result call() throws Exception {
							ConditionalM2 condMoments = conditionalMoments(mean, variance, ranking, maxPtsScale, abseps, releps);
							return new M2Result(condMoments, duplicity);
						}						
					});																							
				}				
				try {
					for (Future<M2Result> future : EstimatorUtils.threadPool.invokeAll(tasks)) {
						M2Result sample = future.get();
						
						for( int j = 0; j < sample.duplicity; j++ ) {
							m1Stats.addValue(sample.result.m1);
							m2Stats.addValue(sample.result.m2);
						}					
						
						currentLL += sample.duplicity * Math.log(sample.result.cdf);
					}
				} catch (InterruptedException | ExecutionException e) {
					throw new RuntimeException(e);				
				}
			}
			else {	
				List<Callable<M1Result>> tasks = new ArrayList<Callable<M1Result>>(counts.entrySet().size());	
				for( Entry<List<Integer>> e : counts.entrySet() ) {
					final int[] ranking = Ints.toArray(e.getElement());	
					final int duplicity = e.getCount();								
																	
					tasks.add(new Callable<M1Result>() {
						@Override
						public M1Result call() throws Exception {
							ConditionalM1 condMoment = conditionalMean(mean, variance, ranking, maxPtsScale, abseps, releps);
							return new M1Result(condMoment, duplicity);
						}					
					});																							
				}				
				try {
					for (Future<M1Result> future : EstimatorUtils.threadPool.invokeAll(tasks)) {
						M1Result sample = future.get();
						
						// Add this ll, expectation a number of times
						for( int j = 0; j < sample.duplicity; j++ ) {
							m1Stats.addValue(sample.result.m1);						
						}					
						
						currentLL += sample.duplicity * Math.log(sample.result.cdf);
					}
				} catch (InterruptedException | ExecutionException e) {
					throw new RuntimeException(e);				
				}																			
			}
			
			// M-step: update mean
			double[] eM1 = m1Stats.getMean();											
			double[] eM2 = null;
			if( floatVariance ) eM2 = m2Stats.getMean();
			
			for( int j = 0; j < m; j++ ) {
				double m1j = eM1[j];
				mean.setEntry(j, m1j);			
				
				if( floatVariance ) variance.setEntry(j, eM2[j] - m1j*m1j);
			}
			
			// Adjust all variables so that first var is 1 		 
			if( floatVariance ) {
				double var = variance.getEntry(0);
				double sd = Math.sqrt(var);
				variance.mapDivideToSelf(var);
				mean.mapDivideToSelf(sd);
			}
			
			// Re-center means - first mean is 0
			mean.mapSubtractToSelf(mean.getEntry(0));
			
			/*
			 * Check out how we did - log likelihood for the old mean is given for free above 
			 * almost 2x speedup over re-computing the LL from scratch			 
			 */								
			double absImpr = currentLL - ll;
			double relImpr = -absImpr / ll;
			ll = lastLL = currentLL;
			
			System.out.printf("Likelihood: %f\n", ll);
			
			if( absImpr < abseps ) {
//				System.out.printf("Absolute tolerance reached: %f < %f\n", absImpr, abseps);
				break;
			}
			if( !Double.isNaN(relImpr) && relImpr < releps ) {
//				System.out.printf("Relative tolerance reached: %f < %f\n", relImpr, releps);
				break;
			}			
		}
		
		// Re-center means so first is 0
		double[] params = mean.toArray();
		double adj = params[0];
		for( int i = 0; i < params.length; i++ )
			params[i] -= adj;		
		return params;
		
//		return mean.toArray();
	}

	private class M1Result {
		final ConditionalM1 result;
		final int duplicity;		
		M1Result(ConditionalM1 result, int duplicity) {
			this.result = result;
			this.duplicity = duplicity;
		}	
	}
	
	private class M2Result {
		final ConditionalM2 result;
		final int duplicity;		
		M2Result(ConditionalM2 result, int duplicity) {
			this.result = result;
			this.duplicity = duplicity;
		}	
	}

	public static class ConditionalM1 {
		public final double cdf;
		public final double[] m1;
		private ConditionalM1(int[] ranking, ExpResult yResult) {
			this.cdf = yResult.cdf;			
			this.m1 = new double[yResult.expValues.length];
			
			// First value is the highest order statistic
			double str = yResult.expValues[0];
			m1[ranking[0]-1] = str;			
			
			// Rest of values assigned by differences
			for( int i = 1; i < m1.length; i++ )
				m1[ranking[i]-1] = (str -= yResult.expValues[i]);			
		}
	}
	
	public static class ConditionalM2 extends ConditionalM1 {
		public final double[] m2;
		private ConditionalM2(int[] ranking, EX2Result yResult) {
			super(ranking, yResult);
			this.m2 = new double[yResult.expValues.length];
			
			// E X_1^2 = E Y_1^2
			double x_i2 = yResult.eX2Values[0];
			m2[ranking[0]-1] = x_i2;
			
			// E X_2^2 = E Y_2^2 - E X_1^2 + 2 E X_2 E X_1
			for( int i = 1; i < m2.length; i++ )
				m2[ranking[i]-1] = (x_i2 = yResult.eX2Values[i] - x_i2 + 2*m1[i]*m1[i-1]);							
		}
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
	public static ConditionalM1 conditionalMean(
			RealVector mean, RealVector variance, int[] ranking, 
			int maxPtsMultiplier, Double abseps, Double releps) {
		MVNParams params = getTransformedParams(mean, variance, ranking);
		ExpResult result = MultivariateNormal.exp(params.mu, params.sigma, params.lower, params.upper, maxPtsMultiplier, abseps, releps);				
		return new ConditionalM1(ranking, result);
	}
	
	/**
	 * Compute the conditional first and second moments for this ranking
	 * 
	 * @param mean
	 * @param variance
	 * @param ranking
	 * @return
	 */
	public static ConditionalM2 conditionalMoments(
			RealVector mean, RealVector variance, int[] ranking, 
			int maxPtsMultiplier, double abseps, double releps) {
		MVNParams params = getTransformedParams(mean, variance, ranking);
		EX2Result result = MultivariateNormal.eX2(params.mu, params.sigma, params.lower, params.upper, maxPtsMultiplier, abseps, releps);				
		return new ConditionalM2(ranking, result);
	}

	public static class MVNParams {
		public final RealVector mu;
		public final RealMatrix sigma;
		public final double[] lower;
		public final double[] upper;
		MVNParams(RealVector mu, RealMatrix sigma, double[] lower, double[] upper) {
			this.mu = mu; 
			this.sigma = sigma;
			this.lower = lower; 
			this.upper = upper;
		}
	}
	
	public static MVNParams getTransformedParams(RealVector mean, RealVector variance, int[] ranking) {
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
		
		return new MVNParams(mu, sigma, lower, upper);
	}

	@Override
	public <T> NormalNoiseModel<T> fitModelOrdinal(PreferenceProfile<T> profile) {
		List<T> ordering = Arrays.asList(profile.getSortedCandidates());
		List<int[]> rankings = profile.getIndices(ordering);		
		
		int m = ordering.size();
		double[] strParams = getParameters(rankings, m);
		NormalNoiseModel<T> nn;
		
		// Create either a fixed or changing variance model
		if ( floatVariance ) {
			double[] sds = lastVariance.map(new Sqrt()).toArray();			
			nn = new NormalNoiseModel<T>(ordering, strParams, sds);				
		} else {
			nn = new NormalNoiseModel<T>(ordering, strParams, Math.sqrt(FIXED_VARIANCE));
		}
		
		nn.setFittedLikelihood(lastLL);
		return nn;		
	}

}
