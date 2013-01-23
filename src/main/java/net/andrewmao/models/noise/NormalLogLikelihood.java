package net.andrewmao.models.noise;

import java.util.List;

import net.andrewmao.probability.BiNormalGenzDist;
import net.andrewmao.probability.MultivariateNormal;
import net.andrewmao.probability.NormalDist;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class NormalLogLikelihood {

	RealVector mean;
	RealVector variance;
	
	/**
	 * Create a likelihood using the vectors as reference parameters.
	 * @param mean
	 * @param variance
	 */
	public NormalLogLikelihood(RealVector mean, RealVector variance) {
		this.mean = mean;
		this.variance = variance;
	}
	
	/**
	 * Create a likelihood using the fixed values given.
	 * @param mean
	 * @param sd
	 */
	public NormalLogLikelihood(double[] mean, double[] sd) {
		
		this.mean = new ArrayRealVector(mean);
		this.variance = new ArrayRealVector(sd.length);
		
		for( int i = 0; i < sd.length; i++ )
			variance.setEntry(i, sd[i] * sd[i]);		
	}
	
	public <T> double logLikelihood(PreferenceProfile<T> candidates, List<T> ordering) {
		List<int[]> indices = candidates.getIndices(ordering);
		return logLikelihood(indices);
	}

	public double logLikelihood(List<int[]> indices) {
		double ll = 0;
		for( int[] ranking : indices )
			ll += singleRankingLL(ranking);
		return ll;
	}

	double singleRankingLL(int[] ranking) {
		if( ranking.length == 2 ) return univariateLL(ranking);
		else if( ranking.length == 3 ) return bivariateLL(ranking);
		else return multivariateLL(ranking);
	}

	/**
	 * Log likelihood computed using multivariate normal cdf
	 * 
	 * @param ranking
	 * @return
	 */
	double multivariateLL(int[] ranking) {
		int n = ranking.length;
		
		// Initialize diagonal variance matrix
		RealMatrix d = new Array2DRowRealMatrix(n, n);
		for( int i = 0; i < n; i++ ) 
			d.setEntry(i, i, variance.getEntry(i));
		
		double[] lower = new double[n-1];
		double[] upper = new double[n-1];
		
		// Compute means and covariances of normal RVs representing differences		
		RealMatrix a = new Array2DRowRealMatrix(n-1, n);
		for( int i = 0; i < n-1; i++ ) {
			a.setEntry(i, ranking[i]-1, 1.0);
			a.setEntry(i, ranking[i+1]-1, -1.0);
						
			upper[i] = Double.POSITIVE_INFINITY;
		}
				
		RealVector mean = a.transpose().preMultiply(this.mean);
		RealMatrix sigma = a.multiply(d).multiply(a.transpose());		
		
		return Math.log(MultivariateNormal.cdf(mean, sigma, lower, upper));		
	}

	double bivariateLL(int[] ranking) {
		double m1 = mean.getEntry(ranking[0]-1);
		double m2 = mean.getEntry(ranking[1]-1);
		double m3 = mean.getEntry(ranking[2]-1);
		double v1 = variance.getEntry(ranking[0]-1);
		double v2 = variance.getEntry(ranking[1]-1);
		double v3 = variance.getEntry(ranking[2]-1);
					
		double mu1 = m2 - m1;
		double mu2 = m3 - m2;
		double sigma1 = Math.sqrt(v1 + v2);
		double sigma2 = Math.sqrt(v2 + v3);
		double rho = -v2 / (sigma1 * sigma2);
		
		double prob = BiNormalGenzDist.cdf(-mu1/sigma1, -mu2/sigma2, rho);
		return Math.log(prob);		
	}

	double univariateLL(int[] ranking) {
		double m1 = mean.getEntry(ranking[0]-1);
		double m2 = mean.getEntry(ranking[1]-1);
		double v1 = variance.getEntry(ranking[0]-1);
		double v2 = variance.getEntry(ranking[1]-1);	
		// prob of x_1 - x_2 > 0 or x_2 - x_1 < 0
		double prob = NormalDist.cdf01((m1 - m2) / Math.sqrt(v1 + v2));  // (0 - (m2 - m1)) / sigma 
		return Math.log(prob);		
	}
	
	/**
	 * Incorrect log likelihood computed using bivariate normal 
	 * @param ranking
	 * @return
	 */
	double bogoLogLikelihood(int[] ranking) {		
		// Special case when ranking is only 2 items long
		if( ranking.length == 2 ) {
			double m1 = mean.getEntry(ranking[0]-1);
			double m2 = mean.getEntry(ranking[1]-1);
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
			double m1 = mean.getEntry(ranking[i]-1);
			double m2 = mean.getEntry(ranking[i+1]-1);
			double m3 = mean.getEntry(ranking[i+2]-1);
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
			double m1 = mean.getEntry(ranking[i]-1);
			double m2 = mean.getEntry(ranking[i+1]-1);
			double v1 = variance.getEntry(ranking[i]-1);
			double v2 = variance.getEntry(ranking[i+1]-1);			
			// prob of x_1 - x_2 > 0 or x_2 - x_1 < 0
			double prob = NormalDist.cdf01((m1 - m2) / Math.sqrt(v1 + v2)); // (0 - (m2 - m1)) / sigma 
			ll -= Math.log(prob);
		}
		
		return ll;
	}
	
}
