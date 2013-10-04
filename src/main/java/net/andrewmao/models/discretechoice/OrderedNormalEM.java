package net.andrewmao.models.discretechoice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang.mutable.MutableDouble;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.primitives.Ints;

import net.andrewmao.models.noise.MeanVarParams;
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
public class OrderedNormalEM extends RandomUtilityEstimator<NormalNoiseModel<?>, MeanVarParams> {	
	
	public static final double FIXED_VARIANCE = 1.0d;
	
	public static final int EM_MAXPTS_MULTIPLIER = 1 << 14;
	
	volatile double lastLL;	
	
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
	public MeanVarParams getParameters(List<int[]> rankings, int numItems) {
		int m = numItems;		
		
		final RealVector mean = new ArrayRealVector(m, 0.0d);
//		RealVector mean = new ArrayRealVector(new NormalDistribution(0,1).sample(m), false);		
		final RealVector variance = new ArrayRealVector(m, FIXED_VARIANCE);
				
		MultivariateMean m1Stats = new MultivariateMean(m), m2Stats = null;
		if( floatVariance ) m2Stats = new MultivariateMean(m);
		double ll = Double.NEGATIVE_INFINITY;
		MutableDouble currentLL = new MutableDouble(0);
		
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
			List<Callable<NormalMoments>> tasks = new ArrayList<Callable<NormalMoments>>(counts.entrySet().size());									
			for( Entry<List<Integer>> e : counts.entrySet() ) {
				final int[] ranking = Ints.toArray(e.getElement());	
				final int weight = e.getCount();								

				tasks.add(new Callable<NormalMoments>() {
					@Override
					public NormalMoments call() throws Exception {
						NormalMoments moments = floatVariance ? 
								conditionalMoments(mean, variance, ranking, maxPtsScale, abseps, releps) :
									conditionalMean(mean, variance, ranking, maxPtsScale, abseps, releps);
						moments.setWeight(weight);
						return moments;							
					}						
				});																							
			}				
			// Reset likelihood and aggregate stats
			currentLL.setValue(0);
			collectData(tasks, m1Stats, m2Stats, currentLL);
			
			// M-step: update mean
			double[] eM1 = m1Stats.getMean();											
			double[] eM2 = floatVariance ? m2Stats.getMean() : null;
			
			for( int j = 0; j < m; j++ ) {
				double m1j = eM1[j];
				mean.setEntry(j, m1j);			
				
				if( floatVariance ) variance.setEntry(j, eM2[j] - m1j*m1j);
			}
			
			// Adjust all variables so that first var is 1 		 
			if( floatVariance ) {
				double var = variance.getEntry(0);				
				variance.mapDivideToSelf(var);
				mean.mapDivideToSelf(Math.sqrt(var));
			}
			
			// Re-center means - first mean is 0
			mean.mapSubtractToSelf(mean.getEntry(0));
			
			/*
			 * Check out how we did - log likelihood for the old mean is given for free above 
			 * almost 2x speedup over re-computing the LL from scratch			 
			 */					
			double newLL = currentLL.doubleValue();
			double absImpr = newLL - ll;
			double relImpr = -absImpr / ll;
			ll = lastLL = newLL;
			
//			System.out.println(mean);
//			System.out.println(variance);
			System.out.printf("Likelihood: %f\n", ll);
						
//			if( i < maxIter ) continue;
			
			if( absImpr < abseps ) {
//				System.out.printf("Absolute tolerance reached: %f < %f\n", absImpr, abseps);
				break;
			}
			if( !Double.isNaN(relImpr) && relImpr < releps ) {
//				System.out.printf("Relative tolerance reached: %f < %f\n", relImpr, releps);
				break;
			}			
		}				
				
		return new MeanVarParams(mean.toArray(), variance.toArray());		
	}

	private void collectData(List<Callable<NormalMoments>> tasks, 
			MultivariateMean m1Stats, MultivariateMean m2Stats, MutableDouble currentLL) {		
		try {
			for (Future<NormalMoments> future : EstimatorUtils.threadPool.invokeAll(tasks)) {
				NormalMoments datum = future.get();
				
				for( int j = 0; j < datum.weight; j++ ) {
					m1Stats.addValue(datum.m1);
					if( floatVariance ) m2Stats.addValue(datum.m2);
				}				
				currentLL.add(datum.weight * Math.log(datum.cdf));
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);				
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
	public static NormalMoments conditionalMean(
			RealVector mean, RealVector variance, int[] ranking, 
			int maxPtsMultiplier, Double abseps, Double releps) {
		MVNParams params = getTransformedParams(mean, variance, ranking);
		ExpResult result = MultivariateNormal.exp(params.mu, params.sigma, params.lower, params.upper, maxPtsMultiplier, abseps, releps);		
		double[] m1 = conditionalM1(ranking, result);		
		return new NormalMoments(m1, result.cdf);
	}
	
	/**
	 * Compute the conditional first and second moments for this ranking
	 * 
	 * @param mean
	 * @param variance
	 * @param ranking
	 * @return
	 */
	public static NormalMoments conditionalMoments(
			RealVector mean, RealVector variance, int[] ranking, 
			int maxPtsMultiplier, double abseps, double releps) {
		MVNParams params = getTransformedParams(mean, variance, ranking);
		EX2Result result = MultivariateNormal.eX2(params.mu, params.sigma, params.lower, params.upper, maxPtsMultiplier, abseps, releps);
		double[] m1 = conditionalM1(ranking, result);
		double[] m2 = conditionalM2(ranking, result, m1);
		return new NormalMoments(m1, m2, result.cdf);
	}

	private static double[] conditionalM1(int[] ranking, ExpResult yResult) {		
		double[] m1 = new double[yResult.expValues.length];
	
		// First value is the highest order statistic
		double str = yResult.expValues[0];
		m1[ranking[0]-1] = str;			
	
		// Rest of values assigned by differences
		for( int i = 1; i < m1.length; i++ )
			m1[ranking[i]-1] = (str -= yResult.expValues[i]);
	
		return m1;
	}

	private static double[] conditionalM2(int[] ranking, EX2Result yResult, double[] m1) {			
		double[] m2 = new double[yResult.expValues.length];
	
		// E X_1^2 = E Y_1^2
		double x_i2 = yResult.eX2Values[0];
		m2[ranking[0]-1] = x_i2;
	
		// E X_2^2 = E Y_2^2 - E X_1^2 + 2 E X_2 E X_1
		/*
		 * FIXME this is incorrect. 
		 * although X_1 and X_2 are marginally uncorrelated,
		 * they are correlated when conditioning on this ranking, so it is wrong.
		 * Must calculate this some other way (probably using sigma matrices) and eve's law
		 */
		for( int i = 1; i < m2.length; i++ )
			m2[ranking[i]-1] = (x_i2 = yResult.eX2Values[i] - x_i2 + 2*m1[i]*m1[i-1]);							
		
		return m2;
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
		DiagonalMatrix d = new DiagonalMatrix(variance.toArray(), false); // No copying necessary
		
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
				
		MeanVarParams params = getParameters(rankings, ordering.size());								
		NormalNoiseModel<T> nn = new NormalNoiseModel<T>(ordering, params);						
		
		nn.setFittedLikelihood(lastLL);
		return nn;		
	}

}
