package net.andrewmao.probability;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;

public class MultivariateNormal {	
	
	static final MvnPackGenz lib = new MvnPackDirect();
	
	// These value errors are unit tested to a certain fail percentage.
	static final DoubleByReference cdf_default_abseps = new DoubleByReference(1e-5);
	static final DoubleByReference cdf_default_releps = new DoubleByReference(1e-5);	
	// These values are a little looser because ANY value could have it
	static final DoubleByReference exp_default_abseps = new DoubleByReference(0.0005);
	static final DoubleByReference exp_default_releps = new DoubleByReference(0.0005);	
	
	public static class CDFResult {
		public final double value;
		public final double error;
		public final boolean converged;
		private CDFResult(double value, double error, boolean converged) {
			this.value = value;
			this.error = error;
			this.converged = converged;
		}		
	}
	
	public static class ExpResult {
		public final double cdf;
		public final double cdfError;
		public final double[] values;
		public final double[] errors;
		public final boolean converged;
		public ExpResult(double cdf, double cdfError,
				double[] values, double[] errors, boolean converged) {
			this.cdf = cdf;
			this.cdfError = cdfError;
			this.values = values;
			this.errors = errors;
			this.converged = converged;
		}			
	}

	public static CDFResult cdf(RealVector mean, RealMatrix sigma, double[] lower, double[] upper) {
		return cdf(mean, sigma, lower, upper, 1, null, null);
	}
	
	public static CDFResult cdf(RealVector mean, RealMatrix sigma, double[] lower, double[] upper,
			int maxTries, Double abseps, Double releps) {				
		int n = checkErrors(mean, sigma, lower, upper);
		double[] correl = getCorrelAdjustLimits(mean, sigma, lower, upper, new double[n]);
		int[] infin = getSetInfin(n, lower, upper);
		
		DoubleByReference abseps_ref = (abseps == null ) ? cdf_default_abseps : new DoubleByReference(abseps);
		DoubleByReference releps_ref = (releps == null ) ? cdf_default_releps : new DoubleByReference(releps);
		
		IntByReference maxpts = new IntByReference(0);		
		DoubleByReference error = new DoubleByReference(0);
		DoubleByReference value = new DoubleByReference(0);
		IntByReference inform = new IntByReference(0);
		
		int tries = 0;
		int pts = (2 << 11) * n;
		int exitCode;
		do {
			maxpts.setValue(pts);			
			lib.mvndst_(new IntByReference(n), lower, upper, infin, correl, 
					maxpts, abseps_ref, releps_ref, error, value, inform);
			exitCode = inform.getValue();
			if( exitCode == 2 )	throw new RuntimeException("Dimension error for MVN");
			pts <<= 1;
		} while( ++tries < maxTries && exitCode > 0 );				
		
		return new CDFResult(value.getValue(), error.getValue(), exitCode == 0);
	}
	
	public static ExpResult exp(RealVector mean, RealMatrix sigma, double[] lower, double[] upper) {
		return exp(mean, sigma, lower, upper, 1, null, null);
	}

	public static ExpResult exp(RealVector mean, RealMatrix sigma, double[] lower, double[] upper,
			int maxTries, Double abseps, Double releps) {
		int n = checkErrors(mean, sigma, lower, upper);
		double[] sds = new double[n];
		double[] correl = getCorrelAdjustLimits(mean, sigma, lower, upper, sds);
		int[] infin = getSetInfin(n, lower, upper);
		
		DoubleByReference abseps_ref = (abseps == null ) ? exp_default_abseps : new DoubleByReference(abseps);
		DoubleByReference releps_ref = (releps == null ) ? exp_default_releps : new DoubleByReference(releps);
		
		IntByReference maxpts = new IntByReference(0);				
		double[] errors = new double[n+1];
		double[] values = new double[n+1];		
		IntByReference inform = new IntByReference(0);										
		
		int tries = 0;
		int pts = (2 << 11) * n;
		int exitCode;
		do {
			maxpts.setValue(pts);			
			lib.mvnexp_(new IntByReference(n), lower, upper, infin, correl, 
					maxpts, abseps_ref, releps_ref, errors, values, inform);
			exitCode = inform.getValue();
			if( exitCode == 2 )	throw new RuntimeException("Dimension error for MVN");
			pts <<= 1;
		} while(  ++tries < maxTries && exitCode > 0 );						
								
		// get just the expected values
		double[] result = new double[n];
		double[] resultErrors = new double[n];
		System.arraycopy(values, 1, result, 0, n);
		System.arraycopy(errors, 1, resultErrors, 0, n);
		
		/* Rescale the expected values and errors 
		 * very important since the computation is on variance 1 normal!
		 */
		for( int i = 0; i < n; i++ ) {
			result[i] = result[i] * sds[i] + mean.getEntry(i);
			resultErrors[i] = resultErrors[i] * sds[i];
		}
		
		return new ExpResult(values[0], errors[0], result, resultErrors, exitCode == 0);
	}

	private static int checkErrors(RealVector mean, RealMatrix sigma,
			double[] lower, double[] upper) {
		int n = mean.getDimension();		
		
		if( n != sigma.getRowDimension() || !sigma.isSquare() )
			throw new RuntimeException("mean and varcov dimensions differ");
		if( n != upper.length || n != lower.length ) 
			throw new RuntimeException("mean and limit dimensions differ");
		
		return n;
	}

	private static double[] getCorrelAdjustLimits(RealVector mean, RealMatrix sigma,
			double[] lower, double[] upper, double[] sd) {
		int n = mean.getDimension();
		
		for( int i = 0; i < n; i++ ) { 
			sd[i] = Math.sqrt(sigma.getEntry(i, i));
			
			if( lower[i] != Double.NEGATIVE_INFINITY ) 
				lower[i] = (lower[i] - mean.getEntry(i))/sd[i];
			if( upper[i] != Double.NEGATIVE_INFINITY )
				upper[i] = (upper[i] - mean.getEntry(i))/sd[i];
		}
		
		double[] correl = new double[n*(n-1)/2];
		
		for( int i = 0; i < n; i++ ) {
			for( int j = 0; j < i; j++ ) {
				correl[(j+1) + (i-1)*i/2 - 1] = sigma.getEntry(i, j) / sd[i] / sd[j];
			}
		}			
		
		return correl;
	}

	private static int[] getSetInfin(int n, double[] lower, double[] upper) {
		int[] infin = new int[n];
		
		for( int i = 0; i < n; i++ ) {
			boolean lowerInf = (lower[i] == Double.NEGATIVE_INFINITY);
			boolean upperInf = (upper[i] == Double.POSITIVE_INFINITY);
			
			if( upperInf && lowerInf ) {
				lower[i] = 0;
				upper[i] = 0;
				infin[i] = -1;				
			}
			else if( lowerInf ) {
				lower[i] = 0;
				infin[i] = 0;				
			}
			else if( upperInf ) {
				upper[i] = 0;
				infin[i] = 1;				
			}			
			else {
				infin[i] = 2;				
			}
		}
		return infin;
	}
	
}
