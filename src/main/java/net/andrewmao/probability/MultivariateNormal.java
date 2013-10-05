package net.andrewmao.probability;

import java.io.IOException;
import java.util.Arrays;

import net.andrewmao.misc.LibraryReplicator;

import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;

public class MultivariateNormal {	
	
	// Load up a thread-safe version of the library when we start
	static final MvnPackGenz lib;	
	static {
		try {
			LibraryReplicator<MvnPackGenz> repl = new LibraryReplicator<MvnPackGenz>(
					MvnPackGenz.class.getClassLoader().getResource(MvnPackGenz.MVNPACK_SO), 
					MvnPackGenz.class, 1); // FIXME: concurrency issues
			lib = repl.getProxiedInterface();
		} catch (IOException e) { 
			throw new RuntimeException(e);			
		}
	}
	
	public static final MultivariateNormal DEFAULT_INSTANCE = new MultivariateNormal();

	static final int MAX_RETRIES = 5;
	
	static final int default_maxpts_multiplier = 1<<15;
	
	// These value errors are unit tested to a certain fail percentage.
	static final double cdf_default_abseps = 1e-5;
	static final double cdf_default_releps = 1e-5;	
	// These values are a little looser because ANY value could have it
	static final double exp_default_abseps = 0.0005;
	static final double exp_default_releps = 0.0005;			 
	
	public final boolean safeComputation;
	public final int maxptsMultiplier;
	public final double cdf_abseps, cdf_releps, exp_abseps, exp_releps;
	
	public MultivariateNormal(int maxPtsMultiplier, 
			double cdf_abseps, double cdf_releps,
			double exp_abseps, double exp_releps, 
			boolean safe) {
		this.maxptsMultiplier = maxPtsMultiplier;
		this.cdf_abseps = cdf_abseps;
		this.cdf_releps = cdf_releps;
		this.exp_abseps = exp_abseps;
		this.exp_releps = exp_releps;
		this.safeComputation = safe;
	}
	
	/**
	 * Create a multivariate normal with default accuracies of
	 * 1e-5 for CDF and 0.0005 for expected values,
	 * and safe computation (will retry until convergence)
	 */
	public MultivariateNormal() {
		this(default_maxpts_multiplier, 
				cdf_default_abseps, cdf_default_releps, 
				exp_default_abseps, exp_default_releps, 
				true);
	}

	public static class CDFResult {
		public final double cdf;
		public final double cdfError;
		public final boolean converged;
		private CDFResult(double cdfValue, double cdfError, boolean converged) {
			this.cdf = cdfValue;
			this.cdfError = cdfError;
			this.converged = converged;			
		}		
	}
	
	public static class ExpResult extends CDFResult {		
		public final double[] expValues;
		public final double[] expErrors;		
		public ExpResult(double cdf, double cdfError,
				double[] expValues, double[] expErrors, boolean converged) {
			super(cdf, cdfError, converged);			
			this.expValues = expValues;
			this.expErrors = expErrors;			
		}			
	}
	
	public static class EX2Result extends ExpResult {
		public final double[] eX2Values;
		public final double[] eX2Errors;
		public EX2Result(double cdf, double cdfError, 
				double[] expValues, double[] expErrors,
				double[] eX2Values, double[] eX2Errors,
				boolean converged) {
			super(cdf, cdfError, expValues, expErrors, converged);
			this.eX2Values = eX2Values;
			this.eX2Errors = eX2Errors;
		}
	}

	public CDFResult cdf(RealVector mean, RealMatrix sigma, double[] lower, double[] upper) {
		CDFResult result = null;
		int iter = 0;
		int maxpts = maxptsMultiplier * mean.getDimension();
		do {			
			if(++iter > MAX_RETRIES || maxpts < 0) throw new ConvergenceException();
//			if( iter > 0 ) System.out.println("Trying again with " + maxpts);
			result = cdf(mean, sigma, lower, upper, maxpts, cdf_abseps, cdf_releps);
			maxpts <<= 1;
		} while(safeComputation && !result.converged);
		return result;
	}
	
	static CDFResult cdf(RealVector mean, RealMatrix sigma, double[] lower, double[] upper,
			int maxPts, double abseps, double releps) {				
		// Copy bounds arrays because we modify them
		double[] adjLower = lower.clone();
		double[] adjUpper = upper.clone();
		int n = checkErrors(mean, sigma, adjLower, adjUpper);
		double[] correl = getCorrelAdjustLimits(mean, sigma, adjLower, adjUpper, new double[n]);		
		int[] infin = getSetInfin(n, adjLower, adjUpper);				
		
		DoubleByReference abseps_ref = new DoubleByReference(abseps);
		DoubleByReference releps_ref = new DoubleByReference(releps);
		
		IntByReference maxpts = new IntByReference(maxPts);		
		DoubleByReference error = new DoubleByReference(0);
		DoubleByReference value = new DoubleByReference(0);
		IntByReference inform = new IntByReference(0);

		lib.mvndst_(new IntByReference(n), adjLower, adjUpper, infin, correl, 
				maxpts, abseps_ref, releps_ref, error, value, inform);
		
		int exitCode = inform.getValue();		
		if( exitCode == 2 )	throw new RuntimeException("Dimension error for MVN");
		
		if( Double.isInfinite(value.getValue()) || Double.isNaN(value.getValue()) ) {
			StringBuilder sb = new StringBuilder();
			sb.append("Error computing CDF; possible concurrent thread access\n");
			sb.append("inform is ").append(exitCode).append("\n");
			sb.append("Mean: ").append(Arrays.toString(mean.toArray())).append("\n");
			sb.append("Sigma: ").append(Arrays.deepToString(sigma.getData())).append("\n");
			sb.append("Lower: ").append(Arrays.toString(lower)).append("\n");
			sb.append("Upper: ").append(Arrays.toString(upper)).append("\n");
			sb.append("Maxpts: ").append(maxPts).append("\n");			
			throw new RuntimeException(sb.toString());
		}
				
		return new CDFResult(value.getValue(), error.getValue(), exitCode == 0);
	}
	
	public ExpResult exp(RealVector mean, RealMatrix sigma, double[] lower, double[] upper) {		
		ExpResult result = null;
		int iter = 0;
		int maxpts = maxptsMultiplier * mean.getDimension();
		do {
			if(++iter > MAX_RETRIES || maxpts < 0) throw new ConvergenceException(); 
//			if( iter > 0 ) System.out.println("Trying again with " + maxpts);
			result = exp(mean, sigma, lower, upper, maxpts, exp_abseps, exp_releps);
			maxpts <<= 1;
		} while(safeComputation && !result.converged);
		return result;
	}

	static ExpResult exp(RealVector mean, RealMatrix sigma, double[] lower, double[] upper,
			int maxPts, double abseps, double releps) {
		// Copy bounds arrays because we modify them
		double[] adjLower = lower.clone();
		double[] adjUpper = upper.clone();
		int n = checkErrors(mean, sigma, adjLower, adjUpper);
		double[] sds = new double[n];
		double[] correl = getCorrelAdjustLimits(mean, sigma, adjLower, adjUpper, sds);		
		int[] infin = getSetInfin(n, adjLower, adjUpper);		
		
		DoubleByReference abseps_ref = new DoubleByReference(abseps);
		DoubleByReference releps_ref = new DoubleByReference(releps);
		
		IntByReference maxpts = new IntByReference(maxPts);				
		double[] errors = new double[n+1];
		double[] values = new double[n+1];		
		IntByReference inform = new IntByReference(0);										
							
		lib.mvnexp_(new IntByReference(n), adjLower, adjUpper, infin, correl, 
				maxpts, abseps_ref, releps_ref, errors, values, inform);
		
		int exitCode = inform.getValue();		
		if( exitCode == 2 ) throw new RuntimeException("Dimension error for MVN");									
								
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

	public EX2Result eX2(RealVector mean, RealMatrix sigma, double[] lower, double[] upper) {		
		EX2Result result = null;
		int iter = 0;
		int maxpts = maxptsMultiplier * mean.getDimension();
		do {
			if(++iter > MAX_RETRIES || maxpts < 0) throw new ConvergenceException();
//			if( iter > 0 ) System.out.println("Trying again with " + maxpts);
			result = eX2(mean, sigma, lower, upper, maxpts, exp_abseps, exp_releps);
			maxpts <<= 1;
		} while(safeComputation && !result.converged);
		return result;
	}
	
	static EX2Result eX2(RealVector mean, RealMatrix sigma, double[] lower, double[] upper,
			int maxPts, double abseps, double releps) {
		// Copy bounds arrays because we modify them
		double[] adjLower = lower.clone();
		double[] adjUpper = upper.clone();
		int n = checkErrors(mean, sigma, adjLower, adjUpper);
		double[] sds = new double[n];
		double[] correl = getCorrelAdjustLimits(mean, sigma, adjLower, adjUpper, sds);		
		int[] infin = getSetInfin(n, adjLower, adjUpper);
		
		DoubleByReference abseps_ref = new DoubleByReference(abseps);
		DoubleByReference releps_ref = new DoubleByReference(releps);
		
		IntByReference maxpts = new IntByReference(maxPts);				
		double[] errors = new double[2*n+1];
		double[] values = new double[2*n+1];						
		IntByReference inform = new IntByReference(0);										
						
		lib.mvnxpp_(new IntByReference(n), adjLower, adjUpper, infin, correl, 
				maxpts, abseps_ref, releps_ref, errors, values, inform);	
		
		int exitCode = inform.getValue();		
		if( exitCode == 2 ) throw new RuntimeException("Dimension error for MVN");									
								
		// Copy over first and second moments
		double[] expResult = new double[n];
		double[] expErrors = new double[n];
		double[] eX2Result = new double[n];
		double[] eX2Errors = new double[n];
		System.arraycopy(values, 1, expResult, 0, n);
		System.arraycopy(errors, 1, expErrors, 0, n);
		System.arraycopy(values, n+1, eX2Result, 0, n);
		System.arraycopy(errors, n+1, eX2Errors, 0, n);
		
		/* Rescaling
		 * The order of operations here is important because we use the first moment to compute the second 
		 */
		for( int i = 0; i < n; i++ ) {
			double var_i = sds[i] * sds[i];
			double mu = mean.getEntry(i);
			double mu_sq = mu * mu;
			/*
			 * Y = sX + u
			 * E[Y^2] = E[s^2X^2 + 2suX + u^2]
			 * Don't touch the first moments yet 
			 * TODO The 2nd moment error calculation is sketchy...
			 */
			eX2Result[i] = var_i * eX2Result[i] + 2 * sds[i] * mu * expResult[i] + mu_sq;
			eX2Errors[i] = var_i * eX2Errors[i] + 2 * sds[i] * mu * expErrors[i];
			/*
			 * E[Y] = E[sX + u]
			 * Now we can modify them
			 */
			expResult[i] = expResult[i] * sds[i] + mean.getEntry(i);
			expErrors[i] = expErrors[i] * sds[i];			
		}
		
		return new EX2Result(values[0], errors[0], expResult, expErrors, eX2Result, eX2Errors, exitCode == 0);
	}
	
	private static int checkErrors(RealVector mean, RealMatrix sigma,
			double[] lower, double[] upper) {
		int n = mean.getDimension();		
		
		if( n != sigma.getRowDimension() || !sigma.isSquare() )
			throw new IllegalArgumentException("mean and varcov dimensions differ");
		if( n != upper.length || n != lower.length ) 
			throw new IllegalArgumentException("mean and limit dimensions differ");
		
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
