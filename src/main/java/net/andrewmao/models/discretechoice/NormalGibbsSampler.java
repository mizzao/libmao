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
class NormalGibbsSampler implements Callable<NormalMoments> {
	
	static final double MAX_IGNORE_PCT = 0.1d;
	static final int MAX_IGNORE = 2000;
	
	Random rnd = new Random();
			
	final int samples, ignored;
		
	final int weight;
	final int[] ranking;
	final int numItems;
	
	final boolean storeVar;
	
	final double[] mus;
	final double[] sigmas;
	
	final MultivariateMean means;
	final MultivariateMean meanSqs;
	
	double[] current;
	
	NormalGibbsSampler(RealVector delta, RealVector variance, int[] ranking, int samples, boolean storeVar, int weight) {
		this.weight = weight;
		this.samples = samples;
		this.ranking = ranking;						
		this.numItems = ranking.length;
		
		this.storeVar = storeVar;
		
		mus = delta.toArray();
		sigmas = variance.toArray();
		for( int i = 0; i < sigmas.length; i++ )
			sigmas[i] = Math.sqrt(sigmas[i]); // Take sqrts here for faster sampling later
		
		means = new MultivariateMean(numItems);
		meanSqs = storeVar ? new MultivariateMean(numItems) : null;	
		
		// Ignore 10% of initial values up to 2000
		this.ignored = (int) Math.round(Math.min(samples * MAX_IGNORE_PCT, MAX_IGNORE));
	}
	
	NormalGibbsSampler(RealVector delta, RealVector variance, int[] ranking, int samples, boolean storeVar) {
		this(delta, variance, ranking, samples, storeVar, 1);
	}
	
	@Override
	public NormalMoments call() {
		/* Initialize sampler with consistent random x_t
		 * Draw uniforms and sort according to values in delta
		 * 
		 * current is a sorted parameter array, not the same ordering as delta 
		 */
		current = new double[numItems];
		
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
		
		double[] rankedM1 = new double[current.length], rankedM2 = null;
		
		if( storeVar ) {
			sampleMeanVar();									
			double[] meanM1 = means.getMean();					
			double[] meanM2 = meanSqs.getMean();
			
			rankedM2 = new double[current.length];
			
			for( int j = 0; j < ranking.length; j++ ) {				
				rankedM1[ranking[j]-1] = meanM1[j];
				rankedM2[ranking[j]-1] = meanM2[j];
			}
		}
		else {
			sampleMean();			
			double[] meanM1 = means.getMean();
			
			for( int j = 0; j < ranking.length; j++ )				
				rankedM1[ranking[j]-1] = meanM1[j];						
		}		
		
		return new NormalMoments(rankedM1, rankedM2, weight);
	}

	private void sampleMeanVar() {		
		double[] sq = new double[current.length];
		
		for( int i = 0; i < samples; i++ ) {
			// r = sorted index to search of
			int r = rnd.nextInt(current.length);
			int idx = ranking[r]-1;
			sample(r, mus[idx], sigmas[idx]);
			
			// Skip the warm-up data
			if( i <= ignored ) continue;			
															
			for( int j = 0; j < current.length; j++ ) {
				double val = current[j];				
				sq[j] = val * val;
			}
			
			means.addValue(current);
			meanSqs.addValue(sq);
		}
	}

	private void sampleMean() {			
		
		for( int i = 0; i < samples; i++ ) {
			// r = sorted index to search of
			int r = rnd.nextInt(current.length);
			int idx = ranking[r]-1;
			sample(r, mus[idx], sigmas[idx]);
			
			// Skip the warm-up data
			if( i <= ignored ) continue;																			
			
			means.addValue(current);
		}
	}

	private void sample(int i, double mu, double sigma) {			
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
		
		// This code not really necessary, probably slows things down
		
//		// Lock value to less than upper			
//		if( i > 0 ) current[i] = Math.min(
//				current[i], 
//				Math.nextAfter(current[i-1], Double.NEGATIVE_INFINITY));
//		// Lock value to greater than lower
//		if( i < current.length - 1 ) current[i] = Math.max(
//				current[i], 
//				Math.nextUp(current[i+1]));
	}
	
}