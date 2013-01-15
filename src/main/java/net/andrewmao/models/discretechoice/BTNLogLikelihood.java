package net.andrewmao.models.discretechoice;

import org.apache.commons.math3.analysis.DifferentiableMultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * Bradley-Terry negative log likelihood with no prior, and first item fixed to strength 0.
 * 
 * @author mao
 *
 */
public class BTNLogLikelihood implements DifferentiableMultivariateFunction {
	
	int[][] wins;
	
	public BTNLogLikelihood(int[][] wins) {
		this.wins = wins;		
		
	}

	@Override
	public double value(double[] point) {
		RealVector theta = new ArrayRealVector(new double[] {0.0}, point);
		
		double value = 0;
		for( int i = 0; i < wins.length; i++ ) {
			for( int j = 0; j < wins.length; j++ ) {
				if( i == j || wins[i][j] == 0 ) continue;
				
				// i beats j, wins[i][j] times
				value += wins[i][j] * Math.log(1 + Math.exp(theta.getEntry(j) - theta.getEntry(i))); 
			}
		}
		
		return value;				
	}

	@Override
	public MultivariateFunction partialDerivative(final int k) {		
		return new MultivariateFunction() {
			@Override
			public double value(double[] point) {
				RealVector theta = new ArrayRealVector(new double[] {0.0}, point);
				
				double value = 0;
				// add wins
				for( int j = 0; j < wins.length; j++ ) {
					// ignore self or no data
					if( j == k+1 || wins[k+1][j] == 0 ) continue;
					
					double prob = Math.exp(theta.getEntry(j) - theta.getEntry(k+1));
					value -= wins[k+1][j]/(1 + prob) * prob;				
				}
				
				// add losses
				for( int i = 0; i < wins.length; i++ ) {
					// ignore self or no data
					if( i == k+1 || wins[i][k+1] == 0 ) continue;
					
					double prob = Math.exp(theta.getEntry(k+1) - theta.getEntry(i));
					value += wins[i][k+1]/(1 + prob) * prob;				
				}
				
				return value;
			}			
		};
	}

	@Override
	public MultivariateVectorFunction gradient() {		
		return new MultivariateVectorFunction() {
			@Override
			public double[] value(double[] point)
					throws IllegalArgumentException {
				RealVector theta = new ArrayRealVector(new double[] {0.0}, point);				
				
				double[] values = new double[point.length];
				
				for( int i = 0; i < wins.length; i++ ) {
					for( int j = 0; j < wins.length; j++ ) {
						if( i == j || wins[i][j] == 0 ) continue;
						
						double prob = Math.exp(theta.getEntry(j) - theta.getEntry(i));
						double value = wins[i][j]/(1+prob) * prob;
						
						if( i > 0 ) values[i-1] -= value;
						if( j > 0 ) values[j-1] += value;
					}
				}
								
				return values;
			}			
		};
	}

}
