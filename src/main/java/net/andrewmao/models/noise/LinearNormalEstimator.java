package net.andrewmao.models.noise;

import java.util.List;

import org.apache.commons.math3.analysis.solvers.LaguerreSolver;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

public class LinearNormalEstimator implements CardinalEstimator<NormalNoiseModel<?>> {

	LaguerreSolver solver = new LaguerreSolver();
	
	final double lambda, eps;
	
	double[] a_last = null;
	
	public LinearNormalEstimator(double lambda, double eps) {
		this.lambda = lambda;
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
				
		double mean = 1d/lambda;
		for( int n = 0; n < a.length; n++ )	
			a[n] = mean;
				
		double nlogLk = nLogLikelihood(delta, a, scores);
		double nlogLk_old = Double.POSITIVE_INFINITY;
		double improvement = (nlogLk_old - nlogLk) / nlogLk_old; 
		
		int iter = 0;
		do {
			// Update strength parameters						
			for( int m = 0; m < delta.length; m++ ) {
				Mean delta_m = new Mean();
				for( int n = 0; n < a.length; n++ )
					delta_m.increment(scores[n][m] / a[n]);					
				delta[m] = delta_m.getResult();
			}
			
			/* 
			 * Update scale values
			 * (First) positive root of quartic
			 */						
			for( int n = 0; n < a.length; n++ ) {
				// Constant term
				double a0 = 0d, a1 = 0d;				
				for( int m = 0; m < delta.length; m++ ) {
					a0 += scores[n][m] * scores[n][m];
					a1 -= delta[m];
				}
				
				double[] coefficients = new double[] { a0, a1, 0, 0, -2*lambda };
				
				Complex root = solver.solveComplex(coefficients, 0);
				if ( root.getImaginary() != 0d ) {
					Complex[] roots = solver.solveAllComplex(coefficients, 0);
					// Take the first positive complex root
					for( Complex r : roots ) {
						if( r.getImaginary() == 0d || r.getReal() > 0d ) {
							root = r;
							break;
						}							
					}
					System.out.println("Could not find positive real root");
					root = null;
				}								
				
				a[n] = (root == null) ? 0 : root.getReal();				
			}
				
			nlogLk_old = nlogLk;
			nlogLk = nLogLikelihood(delta, a, scores);
			System.out.printf("Iteration %d: %.04f\n", ++iter, nlogLk);
			improvement = (nlogLk_old - nlogLk) / nlogLk_old;
			
		} while( improvement > eps );
		
		a_last = a;
		
		return new NormalNoiseModel<T>(items, delta, 1);
	}

	private double nLogLikelihood(double[] d, double[] a, double[][] u) {		
		double nloglk = 0;
		
		for( int n = 0; n < a.length; n++ ) {			
			double denom = 2 * a[n] * a[n];
			
			for( int m = 0; m < d.length; m++ ) {
				double diff = u[n][m] - a[n] * d[m];
				nloglk += diff * diff / denom;
			}
		}
		
		double regularizer = 0;	
		for( int n = 0; n < a.length; n++ ) 
			regularizer += a[n] * a[n];							
		nloglk += lambda * regularizer;
		
		return nloglk;
	}

}
