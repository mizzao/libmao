package net.andrewmao.models.randomutility;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import org.apache.commons.math3.analysis.function.Abs;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import net.andrewmao.probability.TruncatedNormal;
import net.andrewmao.stat.MultivariateMean;
import net.andrewmao.stat.SynchronizedMultivariateMean;

public class NormalMCEM<T> extends MCEMModel<T> {

	final MultivariateMean m1Stats;
	final MultivariateMean m2Stats;
	
	RealVector delta, variance;
	
	protected NormalMCEM(List<T> items) {
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

	@Override
	protected void mStep() {
		/*
		 * M-step: update ranking
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
		
		/* The below adjusts all variables so that
		 * first mean is 0, first var is 1 
		 */
		double var = variance.getEntry(0);
		double sd = Math.sqrt(var);
		
		variance.mapDivideToSelf(var);
		delta.mapDivideToSelf(sd);		
		delta.mapSubtractToSelf(delta.getEntry(0));
		
		System.out.println(delta);
		System.out.println(variance);
		
		// Compute new parameters and log likelihood
		// TODO use range value for integration
		logLikelihood(delta, variance);	
	}
	

	@Override
	protected double[] getCurrentParameters() {
		return delta.toArray();
	}

	private double logLikelihood(RealVector delta, RealVector variance) {
		/* TODO hossein's log likelihood with dynamic programming
		 * log probability of observing such a ranking given the parameters 
		 */
		return 0;
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
			NormalMCEM.super.finishJob();
			
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
