package net.andrewmao.models.discretechoice;

import java.util.List;

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
public class PlackettLuceModel<T> extends RandomUtilityModel<T> {

	static final double tolerance = 1e-9;
	
	public PlackettLuceModel(List<T> items) {
		super(items);		
	}

	@Override
	public ScoredItems<T> getParameters() {
		int m = items.size(); // # of items, indexed by i
		int n = rankings.size(); // # of contests, indexed by j
		
		RealVector diff = null, gamma = new ArrayRealVector(m, 1);		
		
		/*
		 * Compute numerator w = amount of times each candidate placed higher than last in rankings
		 * easy way: subtract 1 for each time someone placed last
		 */
		RealVector w = new ArrayRealVector(m, n);
		for(int[] ranking : rankings)
			w.addToEntry(ranking[m-1]-1, -1.0);		
		
		do {
			double[][] g = new double[n][m];
			for( int j = 0; j < n; j++ ) {
				int[] ranking = rankings.get(j);
				double gsum = 0;
				for( int i = 0; i < m-1; i++ ) {
					gsum += gamma.getEntry(ranking[i]-1);
					g[j][i] = 1/gsum;
				}
				g[j][m-1] = 0;				
			}
			/*
			 * At this point, g[j][i] should be the reciprocal of the
			 * sum of gamma's for places i and higher in the jth contest
			 * except for i=lastplace.
			 */
			
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
		} while( diff.getNorm() > tolerance );
		
		// Return scaled and with log
		double scalar = gamma.getEntry(0);		
		return new ScoredItems<T>(items, gamma.mapDivide(scalar).map(new Log()).toArray());
	}

}
