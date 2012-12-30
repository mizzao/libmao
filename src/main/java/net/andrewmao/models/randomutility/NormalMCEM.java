package net.andrewmao.models.randomutility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.andrewmao.stat.MultivariateMean;
import net.andrewmao.stat.SynchronizedMultivariateMean;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * Implementation of Azari, Parkes, Xia paper on RUM
 * using multithreading
 * 
 * @author mao
 *
 * @param <T>
 */
public class NormalMCEM<T> extends RandomUtilityModel<T> {
	
	List<int[]> rankings;
		
	int iterations;
	double[] start;
	
	final MultivariateMean m1Stats;
	final MultivariateMean m2Stats;
	CountDownLatch latch;
	
	protected NormalMCEM(List<T> items) {
		super(items);		
				
		rankings = new ArrayList<int[]>();
		
		// Concurrent thread accessed
		m1Stats = new SynchronizedMultivariateMean(items.size());
		m2Stats = new SynchronizedMultivariateMean(items.size());		
	}
	
	public void addData(List<T> ranking) {
		int[] ranks = new int[ranking.size()];
		
		int i = 0;
		for( T item : ranking ) {
			ranks[i++] = items.indexOf(item);
		}
		
		rankings.add(ranks);
	}
	
	public void initialize(int iterations, double[] startPoint) {
		this.iterations = iterations;
		this.start = startPoint;
	}
	
	@Override
	public synchronized double[] getParameters() {
		ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
				
		RealVector delta = new ArrayRealVector(start);		
		// TODO: hossein set the variance to different things...
		double[] randomVars = new NormalDistribution().sample(items.size());
		RealVector variance = new ArrayRealVector(randomVars).mapAddToSelf(1);	
		
		for( int i = 0; i < iterations; i++ ) {
			// TODO: where this number come from and why it depends on # iterations?
			int samples = 2000+300*i;
			
			/*
			 * E-step: parallelized Gibbs sampling			
			 */
			m1Stats.clear();
			m2Stats.clear();
			latch = new CountDownLatch(rankings.size());
			
			for( int[] ranking : rankings ) {				
				// TODO proper initialization value here
				exec.submit(new NormalGibbsSampler(samples, ranking, ranking));
			}
			
			// Wait for sampling to finish
			while( latch.getCount() > 0 ) {
				try { latch.await();
				} catch (InterruptedException e) {}				
			}
			
			/*
			 * M-step: update ranking
			 */
			delta = new ArrayRealVector(m1Stats.getMean());
			variance = new ArrayRealVector(m2Stats.getMean()).subtract(delta.ebeMultiply(delta));
			
			// TODO: what does this do?
			delta.setEntry(0, 1);
			variance.setEntry(0, 1);
			
			// Compute new parameters and log likelihood
			// TODO use range value
			logLikelihood(delta, variance);			
		}
		
		exec.shutdown();
		
		return null;
	}

	private double logLikelihood(RealVector delta, RealVector variance) {
		// TODO hossein's log likelihood with dynamic programming
		return 0;
	}

	class NormalGibbsSampler implements Runnable {		
		Random rnd = new Random();
		
		int samples, ignored;
		int[] start, ranking;
		
		MultivariateMean means;
		MultivariateMean meanSqs;		
		
		NormalGibbsSampler(int samples, int[] start, int[] ranking) {
			this.samples = samples;
			this.start = start;
			this.ranking = ranking;
			
			means = new MultivariateMean(start.length);
			meanSqs = new MultivariateMean(start.length);			
			
			// 10% of initial values ignored?
			this.ignored = (int) Math.round(1.0 * samples / 10);
		}
	
		@Override
		public void run() {
			int[] current = start.clone();
			
			for( int i = 0; i < samples; i++ ) {
				int r = 1 + rnd.nextInt(current.length);
				current = sample(r, current, ranking);
				
				// Skip the warm-up data
				if( i <= ignored ) continue;
				
				means.addValue(current);
				double[] sq = new double[current.length];				
				for( int j = 0; j < current.length; j++ )				
					sq[j] = current[j] * current[j];
				meanSqs.addValue(sq);
			}			
			
			m1Stats.addValue(means.getMean());
			m2Stats.addValue(meanSqs.getMean());
			// record values added
			latch.countDown();
		}

		private int[] sample(int r, int[] current, int[] ranking) {
			// TODO One step of the gibbs sampling
			return null;
		}	
	}

}
