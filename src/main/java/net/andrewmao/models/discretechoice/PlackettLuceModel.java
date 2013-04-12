package net.andrewmao.models.discretechoice;

import java.util.Arrays;
import java.util.List;

import net.andrewmao.models.noise.GumbelNoiseModel;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.apache.commons.math3.analysis.function.Log;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * PLMM algorithm from 
 * http://sites.stat.psu.edu/~dhunter/code/btmatlab/plackmm.m
 * 
 * TODO add partial orders; only takes care of total orders for now
 * 
 * @author mao
 *
 * @param <T>
 */
public class PlackettLuceModel extends RandomUtilityEstimator<GumbelNoiseModel<?>> {
	
	static final double PL_MAX_ITERS = 500;
	
	static final double ll_tolerance = 1e-5;
	static final double param_tolerance = 1e-9;	
	
	final boolean failMM;
	
	volatile double lastComputedLL;
	
	public PlackettLuceModel(boolean failMM) {
		this.failMM = failMM;
	}
	
	@Override
	public double[] getParameters(List<int[]> rankings, int numItems) {
		int m = numItems; // # of items, indexed by i
		int n = rankings.size(); // # of contests, indexed by j
		
		RealVector diff = null, gamma = new ArrayRealVector(m, 1);		
		
		/*
		 * Compute numerator w = amount of times each candidate placed higher than last in rankings
		 * easy way: subtract 1 for each time someone placed last
		 */
		RealVector w = new ArrayRealVector(m, n);
		for(int[] ranking : rankings)
			w.addToEntry(ranking[m-1]-1, -1.0);
		
		int iter = 0;
		boolean cont = true;
		double lastLL = Double.NEGATIVE_INFINITY, absImpr, relImpr;
		
		do {
			if( iter++ > PL_MAX_ITERS && failMM ) 
				throw new RuntimeException("MM failed to converge...check for MM assumption satisfied, or use LL convergence instead.");
			
			double[][] g = new double[n][m];
			for( int j = 0; j < n; j++ ) {
				int[] ranking = rankings.get(j);
				
				double gsum = 0;
				for( int i = m-1; i >= 0; i-- ) {
					gsum += gamma.getEntry(ranking[i]-1);					
					if( i == m-1 ) continue;
					g[j][i] = 1/gsum;
				}				
			}
			/*
			 * At this point, g[j][i] should be the reciprocal of the
			 * sum of gamma's for places i and higher in the jth contest
			 * except for i=lastplace.
			 */
			
			double ll = w.dotProduct(gamma.map(new Log()));
			for( int i = 0; i < g.length; i++ )
				for( int j = 0; j < g[i].length; j++ )
					if( g[i][j] > 0 ) ll += Math.log(g[i][j]);
//			System.out.println("Log likelihood: " + ll);			
			
			absImpr = ll - lastLL;
			relImpr = -absImpr / lastLL;
			lastLL = lastComputedLL = ll;
			
			for( int j = 0; j < n; j++ ) {
				double cumsum = 0;
				for( int i = 0; i < m; i++ ) {
					cumsum += g[j][i];
					g[j][i] = cumsum;
				}
			}
			/*
			 * Now g[j][i] should be the sum of all the denominators
			 * for ith place in the jth contest.
			 */
			
			/* 
			 * Now for the denominator in gamma(i), we need to 
			 * add up all the g[j][r(i,j)] for nonzero r(i,j).
			 * 
			 * where r(i,j) is the place of item i in contest j.
			 */
			double[] denoms = new double[m];			
			for( int j = 0; j < rankings.size(); j++ ) {
				int[] ranking = rankings.get(j);
				for( int i = 0; i < m; i++ ) {
					denoms[ranking[i]-1] += g[j][i];
				}				
			}							
			
			@SuppressWarnings("deprecation")
			RealVector newGamma = w.ebeDivide(new ArrayRealVector(denoms));			
			diff = newGamma.subtract(gamma); 
			gamma = newGamma;			
			
			cont = ( failMM && diff.getNorm() > param_tolerance) || 
					(!failMM && iter < PL_MAX_ITERS && (Double.isNaN(relImpr) || relImpr > ll_tolerance) );						
		} while( cont );
		
		// Return scaled and with log
		double scalar = gamma.getEntry(0);
		return gamma.mapDivide(scalar).map(new Log()).toArray();
	}

	@Override
	public <T> GumbelNoiseModel<T> fitModelOrdinal(PreferenceProfile<T> profile) {
		List<T> ordering = Arrays.asList(profile.getSortedCandidates());				
		List<int[]> rankings = profile.getIndices(ordering);		
		
		double[] strParams = getParameters(rankings, ordering.size());
		
		GumbelNoiseModel<T> gnm = new GumbelNoiseModel<T>(ordering, strParams);
		gnm.setFittedLikelihood(lastComputedLL);
		return gnm;
	}

}
