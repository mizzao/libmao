package net.andrewmao.models.discretechoice;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import org.apache.commons.math3.analysis.function.Abs;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import net.andrewmao.probability.BiNormalGenzDist;
import net.andrewmao.probability.NormalDist;
import net.andrewmao.probability.TruncatedNormal;
import net.andrewmao.stat.MultivariateMean;
import net.andrewmao.stat.SynchronizedMultivariateMean;

/**
 * This is a general implementation of the probit model as described at
 * https://wiki.ece.cmu.edu/ddl/index.php/Introduction_to_random_utility_discrete_choice_models
 * 
 * @author mao
 *
 * @param <T>
 */
public class OrderedNormalMCEM<T> extends MCEMModel<T> {

	final MultivariateMean m1Stats;
	final MultivariateMean m2Stats;
	
	RealVector delta, variance;
	
	public OrderedNormalMCEM(List<T> items) {
		super(items);
				
		// Concurrent thread accessed
		m1Stats = new SynchronizedMultivariateMean(items.size());
		m2Stats = new SynchronizedMultivariateMean(items.size());	
	}

	@Override
	protected void initialize() {		
		delta = new ArrayRealVector(start);		
		// Can either initialize variance randomly or fixed 
		double[] randomVars = new NormalDistribution().sample(items.size());
		variance = new ArrayRealVector(randomVars).mapToSelf(new Abs()).mapAddToSelf(1);	
	}

	@Override
	protected void eStep(int i) {
		/*
		 * E-step: parallelized Gibbs sampling			
		 */
		
		// TODO: where this number come from and why it depends on # iterations?
		int samples = 2000+300*i;
				
		m1Stats.clear();
		m2Stats.clear();
		super.beginNumJobs(rankings.size());
		
		for( int[] ranking : rankings ) {				
			super.addJob(new NormalGibbsSampler(samples, ranking));
		}		
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void mStep() {
		/*
		 * M-step: re-compute parameters
		 */
		delta = new ArrayRealVector(m1Stats.getMean());		
		variance = new ArrayRealVector(m2Stats.getMean()).subtract(delta.ebeMultiply(delta));
					
		/* adjust the mean and variance values to prevent drift:
		 * first subtract means so that first value is 0
		 * TODO then scale variance to 1
		 */
				
		// Dunno what hossein was thinking with this, doesn't seem to work well
//		delta.setEntry(0, 1); 
//		variance.setEntry(0, 1);
		
		// Testing to see if parameters converge
//		variance.set(1);		
		
		/* The below adjusts all variables so that
		 * first mean is 0, first var is 1 
		 */
		double var = variance.getEntry(0);
		double sd = Math.sqrt(var);
		
		variance.mapDivideToSelf(var);
		delta.mapDivideToSelf(sd);		
		delta.mapSubtractToSelf(delta.getEntry(0));
		
//		System.out.println(delta);
//		System.out.println(variance);		
	}
	

	@Override
	protected double[] getCurrentParameters() {
		return delta.toArray();
	}

	public double getLogLikelihood() {		
		double ll = 0;		
		for( int[] ranking : rankings )	ll += logLikelihood(ranking);				
		return ll;
	}
	
	private double logLikelihood(int[] ranking) {		
		// Special case when ranking is only 2 items long
		if( ranking.length == 2 ) {
			double m1 = delta.getEntry(ranking[0]-1);
			double m2 = delta.getEntry(ranking[1]-1);
			double v1 = variance.getEntry(ranking[0]-1);
			double v2 = variance.getEntry(ranking[1]-1);			
			// prob of x_1 - x_2 > 0 or x_2 - x_1 < 0
			double prob = NormalDist.cdf01((m1 - m2) / Math.sqrt(v1 + v2));  // (0 - (m2 - m1)) / sigma 
			return Math.log(prob);
		}
		
		/* log likelihood as computed using
		 * http://math.stackexchange.com/questions/270745/compute-probability-of-a-particular-ordering-of-normal-random-variables  
		 */
		double ll = 0;
		
		// add all bivariate triples
		for( int i = 0; i < ranking.length - 2; i++ ) {
			/* joint probability of (x_i > x_i+1, x_i+1 > x_i+2 )
			 * equal to (x_i+1 - x_i < 0, x_i+2 - x_i+1 < 0)
			 */
			double m1 = delta.getEntry(ranking[i]-1);
			double m2 = delta.getEntry(ranking[i+1]-1);
			double m3 = delta.getEntry(ranking[i+2]-1);
			double v1 = variance.getEntry(ranking[i]-1);
			double v2 = variance.getEntry(ranking[i+1]-1);
			double v3 = variance.getEntry(ranking[i+2]-1);
						
			double mu1 = m2 - m1;
			double mu2 = m3 - m2;
			double sigma1 = Math.sqrt(v1 + v2);
			double sigma2 = Math.sqrt(v2 + v3);
			double rho = -v2 / (sigma1 * sigma2);
			
			double prob = BiNormalGenzDist.cdf(-mu1/sigma1, -mu2/sigma2, rho);
			ll += Math.log(prob);
		}
		
		// subtract all pairs that are not at the beginning of end
		for( int i = 1; i < ranking.length - 2; i++ ) {
			double m1 = delta.getEntry(ranking[i]-1);
			double m2 = delta.getEntry(ranking[i+1]-1);
			double v1 = variance.getEntry(ranking[i]-1);
			double v2 = variance.getEntry(ranking[i+1]-1);			
			// prob of x_1 - x_2 > 0 or x_2 - x_1 < 0
			double prob = NormalDist.cdf01((m1 - m2) / Math.sqrt(v1 + v2)); // (0 - (m2 - m1)) / sigma 
			ll -= Math.log(prob);
		}
		
		return ll;
	}

	class NormalGibbsSampler implements Callable<Void> {		
		Random rnd = new Random();
		
		int samples, ignored;
		int[] ranking;
		
		MultivariateMean means;
		MultivariateMean meanSqs;		
		
		NormalGibbsSampler(int samples, int[] ranking) {
			this.samples = samples;
			this.ranking = ranking;			
			
			means = new MultivariateMean(items.size());
			meanSqs = new MultivariateMean(items.size());	
			
			// 10% of initial values ignored?
			this.ignored = (int) Math.round(samples / 10.0d);
		}
	
		@Override
		public Void call() {
			/* Initialize sampler with consistent random x_t
			 * Draw uniforms and sort according to values in delta
			 * 
			 * current is a sorted parameter array, not the same ordering as delta 
			 */
			double[] current = new double[items.size()];
			
			int c = ranking.length;
			double[] rands = new double[c];			
			for( int i = 0; i < c; i++ )
				rands[i] = rnd.nextDouble();
			Arrays.sort(rands);
			/* Put random values from greatest to least, 0..c-1
			 * based on values in delta
			 */
			for( int i = 0; i < c; i++ )
				current[c-i-1] = rands[i];
			
			for( int i = 0; i < samples; i++ ) {
				// r = sorted index to search of
				int r = rnd.nextInt(current.length);
				sample(r, current, 
						delta.getEntry(ranking[r]-1),
						Math.sqrt(variance.getEntry(ranking[r]-1)));
				
				// Skip the warm-up data
				if( i <= ignored ) continue;
				
				double[] mean = new double[current.length];
				double[] sq = new double[current.length];
																
				for( int j = 0; j < current.length; j++ ) {
					double val = current[j];
					mean[ranking[j]-1] = val;
					sq[ranking[j]-1] = val * val;
				}
				
				means.addValue(mean);
				meanSqs.addValue(sq);
			}			
			
			m1Stats.addValue(means.getMean());
			m2Stats.addValue(meanSqs.getMean());
			
			// record values added
			OrderedNormalMCEM.super.finishJob();
			
			return null;
		}

		private void sample(int i, double[] current, double mu, double sigma) {			
			/*
			 * One step of the gibbs sampling
			 * Truncated normal sample of the value at index i
			 * 
			 * TODO properly take care of partial orders
			 */
			
			double lower = (i < current.length - 1) ? current[i+1] : Double.NEGATIVE_INFINITY;
			double upper = (i > 0) ? current[i-1] : Double.POSITIVE_INFINITY;
			
			TruncatedNormal tn = new TruncatedNormal(mu, sigma, lower, upper);
			
			current[i] = tn.sample();
			
			// Lock value to less than upper			
			if( i > 0 ) current[i] = Math.min(current[i], 
					Math.nextAfter(current[i-1], Double.NEGATIVE_INFINITY));
			// Lock value to greater than lower
			if( i < current.length - 1 ) current[i] = Math.max(current[i], 
					Math.nextUp(current[i+1]));				
		}	
	}

}
