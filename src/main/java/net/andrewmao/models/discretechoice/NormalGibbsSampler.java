package net.andrewmao.models.discretechoice;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;

import net.andrewmao.probability.TruncatedNormal;
import net.andrewmao.probability.TruncatedNormalQuick;
import net.andrewmao.stat.MultivariateMean;

import org.apache.commons.math3.linear.RealVector;

/**
 * Gibbs sampler to compute conditional expectation of normal random variables
 * given a ranking. 
 * 
 * @author mao
 *
 */
class NormalGibbsSampler implements Callable<net.andrewmao.models.discretechoice.NormalGibbsSampler.NormalMoments> {		
	Random rnd = new Random();
			
	int samples, ignored;
	
	final int weight;
	final int[] ranking;
	final int numItems;
	
	final double[] mus;
	final double[] sigmas;
	
	MultivariateMean means;
	MultivariateMean meanSqs;		
	
	NormalGibbsSampler(RealVector delta, RealVector variance, int[] ranking, int samples, int weight) {
		this.weight = weight;
		this.samples = samples;
		this.ranking = ranking;						
		this.numItems = ranking.length;
		
		mus = delta.toArray();
		sigmas = variance.toArray();
		for( int i = 0; i < sigmas.length; i++ )
			sigmas[i] = Math.sqrt(sigmas[i]); // Take sqrts here for faster sampling later
		
		means = new MultivariateMean(numItems);
		meanSqs = new MultivariateMean(numItems);	
		
		// 10% of initial values ignored?
		this.ignored = (int) Math.round(samples / 10.0d);
	}
	
	NormalGibbsSampler(RealVector delta, RealVector variance, int[] ranking, int samples) {
		this(delta, variance, ranking, samples, 1);
	}
	
	static class NormalMoments {
		final double[] m1;
		final double[] m2;
		final int weight;
		private NormalMoments(double[] m1, double[] m2, int weight) {
			this.m1 = m1;
			this.m2 = m2;
			this.weight = weight;
		}
	}
	
	@Override
	public NormalMoments call() {
		/* Initialize sampler with consistent random x_t
		 * Draw uniforms and sort according to values in delta
		 * 
		 * current is a sorted parameter array, not the same ordering as delta 
		 */
		double[] current = new double[numItems];
		
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
			sample(r, current, mus[ranking[r]-1], sigmas[ranking[r]-1]);
			
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
		
		return new NormalMoments(means.getMean(), meanSqs.getMean(), weight);
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
		
		TruncatedNormal tn = new TruncatedNormalQuick(mu, sigma, lower, upper);
		
		current[i] = tn.sample();
		
		// Lock value to less than upper			
		if( i > 0 ) current[i] = Math.min(current[i], 
				Math.nextAfter(current[i-1], Double.NEGATIVE_INFINITY));
		// Lock value to greater than lower
		if( i < current.length - 1 ) current[i] = Math.max(current[i], 
				Math.nextUp(current[i+1]));				
	}
	
}