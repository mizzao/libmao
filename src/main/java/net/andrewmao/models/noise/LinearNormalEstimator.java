package net.andrewmao.models.noise;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.analysis.solvers.LaguerreSolver;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

public class LinearNormalEstimator implements CardinalEstimator<NormalNoiseModel<?>> {

	LaguerreSolver solver = new LaguerreSolver();
	
	final double norm, eps;
	
	double[] a_last = null;
	
	public LinearNormalEstimator(double norm, double eps) {
		this.norm = norm;
		this.eps = eps;
	}
	
	@Override
	public <T> NormalNoiseModel<T> fitModelCardinal(List<T> items, double[][] scores) {
		/*
		 * Implemented from Hossein's 4/12/13 model with scaled variances
		 */
		
		// Initialize a_n and delta_m
		double[] delta = new double[items.size()];
		double[] a = new double[scores.length];
						
		for( int i = 0; i < a.length; i++ )	
			a[i] = 1d;
		
		Mean a_mean = new Mean();
		double a_hat = a_mean.evaluate(a);
		
		double nlogLk = nLogLikelihood(delta, // a_hat,
				a, scores);
		double nlogLk_old = Double.POSITIVE_INFINITY;
		double improvement = (nlogLk_old - nlogLk) / nlogLk_old; 
		
		int iter = 0;
		do {
			// Update strength parameters												
			updateDelta(delta, a, scores);
			
			a_hat = a_mean.evaluate(a);
			
			updateAlpha(a, delta, scores);
				
			nlogLk_old = nlogLk;
			nlogLk = nLogLikelihood(delta, // a_hat,
					a, scores);
//			System.out.printf("Iteration %d: %.04f, a: %.04f, mu: %s\n", ++iter, nlogLk, a_hat, Arrays.toString(delta));
			improvement = (nlogLk_old - nlogLk) / nlogLk_old;
			
		} while( Math.abs(improvement) > eps );
		
		a_last = a;
		
		return new NormalNoiseModel<T>(items, delta, 1);
	}

	void updateDelta(double[] delta, double[] a, double[][] scores) {
		double delta_sum = 0;
		
		for( int j = 0; j < delta.length; j++ ) {
			Mean delta_m = new Mean();
			for( int i = 0; i < a.length; i++ )
				delta_m.increment(scores[i][j] / a[i]);					
			delta_sum += delta[j] = delta_m.getResult();
		}
		
		for( int j = 0; j < delta.length; j++ ) {
			delta[j] *= norm / delta_sum;
		}
	}

	void updateAlpha(double[] a, double[] delta, double[][] scores) {
			/* 
			 * Update scale values
			 * (First) positive root of quartic
			 */						
			for( int i = 0; i < a.length; i++ ) {
				// Constant term
				double a0 = 0d, a1 = 0d;
				
				for( int j = 0; j < delta.length; j++ ) {
					a0 -= scores[i][j] * scores[i][j];
					a1 += delta[j] * scores[i][j];
				}								
				
				double[] coefficients = new double[] { a0, a1 }; // , 0, -lambda * a_hat, lambda };
								
	//				Complex root = solver.solveComplex(coefficients, mean);
				
				Complex root = null;
					
				Complex[] roots = solver.solveAllComplex(coefficients, 0);
				// Take the (only) positive complex root
				for( Complex r : roots ) {
					if( rootIsNonnegativeReal(r) ) {
						if( root != null )
							throw new RuntimeException(String.format("Found two positive real roots: %s and %s", root, r));						
						
						root = r;
						break;
					}							
				}
	
				if( root == null ) {
					throw new RuntimeException("Could not find positive real root; roots are " + Arrays.toString(roots));								
				}
				else {
					a[i] = root.getReal();	
				}															
								
			}
		}

	static boolean rootIsNonnegativeReal(Complex root) {
		return Math.abs(root.getImaginary()) < 1e-10 && root.getReal() >= -Double.MIN_NORMAL;
	}

	private double nLogLikelihood(double[] mu, // double a_hat, 
			double[] a, double[][] u) {		
		double nloglk = 0;
		
		for( int i = 0; i < a.length; i++ ) {			
			double denom_i = 2 * a[i] * a[i];
			
			double sum_i = 0;
			
			for( int j = 0; j < mu.length; j++ ) {
				double diff = u[i][j] - a[i] * mu[j];
				sum_i += diff * diff;
			}
			
			nloglk += sum_i / denom_i;
		}
		
//		double regularizer = 0;	
//		for( int i = 0; i < a.length; i++ ) {
//			double diff = (a[i] - a_hat);
//			regularizer += diff * diff;		
//		}
//		nloglk += lambda * regularizer / 2;
		
		return nloglk;
	}

}
