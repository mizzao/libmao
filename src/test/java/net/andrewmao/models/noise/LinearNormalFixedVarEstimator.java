package net.andrewmao.models.noise;

import java.util.List;

public class LinearNormalFixedVarEstimator implements CardinalEstimator<NormalNoiseModel<?>> {

	final double lambda, eps;
	
	double[] a_last = null;
	
	public LinearNormalFixedVarEstimator(double lambda, double eps) {
		this.lambda = lambda;
		this.eps = eps;
	}
	
	@Override
	public <T> NormalNoiseModel<T> fitModelCardinal(List<T> items, double[][] scores) {
		/*
		 * Implemented from Hossein's 4/02/13 model
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
		
//		int iter = 0;
		do {
			double denom_d = 0;
			for( int n = 0; n < a.length; n++ ) denom_d += a[n] * a[n];
			
			for( int m = 0; m < delta.length; m++ ) {
				double num_d = 0;
				for( int n = 0; n < a.length; n++ )
					num_d += a[n] * scores[n][m];
				delta[m] = num_d / denom_d;
			}
			
			double denom_a = 0;
			for( int m = 0; m < delta.length; m++ ) denom_a += delta[m] * delta[m];
			
			for( int n = 0; n < a.length; n++ ) {
				double num_a = 0;
				for( int m = 0; m < delta.length; m++ )
					num_a += delta[m] * scores[n][m];
				
				a[n] = Math.max(0, (num_a - lambda) / denom_a);				
			}
			
			// Normalize a[n] so that mean is 1/lambda;
//			Mean alpha_mean = new Mean();
//			alpha_mean.evaluate(a);
//			double normalization = alpha_mean.getResult() * lambda;			
//			for( int n = 0; n < a.length; n++ ) a[n] /= normalization;
			
			nlogLk_old = nlogLk;
			nlogLk = nLogLikelihood(delta, a, scores);
//			System.out.printf("Iteration %d: %.04f\n", ++iter, nlogLk);
			improvement = (nlogLk_old - nlogLk) / nlogLk_old;
		} while( improvement > eps );
		
		a_last = a;
		
		return new NormalNoiseModel<T>(items, delta, 1);
	}

	private double nLogLikelihood(double[] d, double[] a, double[][] u) {		
		double nloglk = 0;
		
		for( int n = 0; n < a.length; n++ ) {
			for( int m = 0; m < d.length; m++ ) {
				double diff = u[n][m] - a[n] * d[m];
				nloglk += diff * diff / 2;
			}
		}
		
		double p = 0;	
		for( int n = 0; n < a.length; n++ ) p += a[n];							
		nloglk += lambda * p;
		
		return nloglk;
	}

}
