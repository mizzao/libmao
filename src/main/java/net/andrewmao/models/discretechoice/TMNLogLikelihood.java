package net.andrewmao.models.discretechoice;

import java.util.Arrays;

import net.andrewmao.probability.NormalDist;

import org.apache.commons.math3.analysis.DifferentiableMultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixChangingVisitor;
import org.apache.commons.math3.linear.RealVector;

/**
 * Thurstone-Mosteller negative log likelihood with first parameter fixed to 0 
 * to avoid overparametrization
 * 
 * TODO Make consistent the gradients/hessian here from fixing the first parameter.
 * The original TM code used the Powell Optimizer, which ignores the gradient, so probably didn't affect previous uses.
 * 
 * @author Mao
 *
 */
@SuppressWarnings("deprecation")
public class TMNLogLikelihood implements DifferentiableMultivariateFunction {	
	
	RealMatrix mat;
	
	public TMNLogLikelihood(RealMatrix mat) {
		this.mat = mat;
	}
	
	/**
	 * Calculate the log likelihood of matrix @mat given strength estimate @point
	 */
	@Override
	public double value(final double[] point)
			throws IllegalArgumentException {

		// Add 0 to the front of the array
		int actualLength = point.length + 1;
		RealVector pointNew = new ArrayRealVector(new double[] { 0.0 }, point);		

		double ll = 0;
		
		for (int i = 0; i < actualLength; i++) {
			for (int j = 0; j < actualLength; j++) {
				if (i == j)	continue;
				
				ll -= mat.getEntry(i, j) * Math.log(NormalDist.cdf01(pointNew.getEntry(i) - pointNew.getEntry(j)));
			}
		}		

		return ll;
	}

	/**
	 * Compute the partial derivative of the log likelihood function
	 */
	public MultivariateFunction partialDerivative(final int k) {
		return new MultivariateFunction() {
			@Override
			public double value(double[] point)	throws IllegalArgumentException {				
				// Add 0 to the front of the array
				int actualLength = point.length + 1;
				RealVector pointNew = new ArrayRealVector(new double[] { 0.0 }, point);
				int i = k+1;

				double value = 0;
				
				for (int j = 0; j < actualLength; j++) {	
					if( i == j ) continue; 
					
					double i_minus_j = pointNew.getEntry(i) - pointNew.getEntry(j);					
					double density = NormalDist.density01(i_minus_j);
					double phi_i_j = NormalDist.cdf01(i_minus_j);
					
					value += mat.getEntry(i, j)
							* density // NormalDist.density01(i_minus_j)
							/ phi_i_j; // NormalDist.cdf01(i_minus_j)
					value -= mat.getEntry(j, i)
							* density // symmetric - NormalDist.density01(j_minus_i)
							/ (1 - phi_i_j); // NormalDist.cdf01(j_minus_i);
				}
				// negative log likelihood
				return (-1)*value;
			}
		};
	}

	/**
	 * Compute the gradient of the log likelihood function
	 */
	public MultivariateVectorFunction gradient() {
		return new MultivariateVectorFunction() {
			@Override
			public double[] value(double[] point) 
					throws IllegalArgumentException {

				// Add 0 to the front of the array
				int actualLength = point.length + 1;
				RealVector pointNew = new ArrayRealVector(new double[] { 0.0 }, point);
				
				double[] values = new double[actualLength];
				Arrays.fill(values, 0.0);
				
				for (int i = 0; i < actualLength; i++) {
					for (int j = 0; j < actualLength; j++) {
						if (i == j) continue;
						
						double i_minus_j = pointNew.getEntry(i) - pointNew.getEntry(j);
						double density = NormalDist.density01(i_minus_j);
						double phi_i_j = NormalDist.cdf01(i_minus_j);

						values[i] += mat.getEntry(i, j)
								* density // NormalDist.density01(i_minus_j)
								/ phi_i_j; // NormalDist.cdf01(i_minus_j)
						values[i] -= mat.getEntry(j, i)
								* density // symmetric - NormalDist.density01(j_minus_i)
								/ (1 - phi_i_j); // NormalDist.cdf01(j_minus_i);
					}
					// we're still doing negative log likelihood
					values[i] = (-1)* values[i];
				}
				
				double[] gradient = Arrays.copyOfRange(values, 1, actualLength);
				return gradient;
			}
		};
	}

	/**
	 * Compute the 2nd order derivatives of the log likelihood function
	 * 
	 * This is the general hessian of the whole likelihood, not the partial one above.
	 */
	public RealMatrix hessian(RealVector est) {
		int actualLength = est.getDimension() + 1;
		RealVector estNew = new ArrayRealVector(new double[] {0.0});
		estNew = estNew.append(est);
		
		RealMatrix hessian = new Array2DRowRealMatrix(actualLength, actualLength);
		for (int i = 0; i < actualLength; i++) {
			for (int j = 0; j < actualLength; j++) {
				if (i == j)	continue;
				
				double r1 = NormalDist.density01(estNew.getEntry(i) - estNew.getEntry(j))
						   	/ NormalDist.cdf01(estNew.getEntry(i) - estNew.getEntry(j));
				double r2 = NormalDist.density01(estNew.getEntry(j) - estNew.getEntry(i))
						   	/ NormalDist.cdf01(estNew.getEntry(j) - estNew.getEntry(i));
				
				// 2nd derivative of (i,j), i is not equal to j
				double valueij = mat.getEntry(i, j) * r1 * (estNew.getEntry(i) - estNew.getEntry(j) + r1)
							   + mat.getEntry(j, i) * r2 * (estNew.getEntry(j) - estNew.getEntry(i) + r2); 
				hessian.setEntry(i,j, valueij);
				
				// 2nd derivative of (i,i)
				double valueii = hessian.getEntry(i, i) 
						- mat.getEntry(i, j) * r1 * (estNew.getEntry(i) - estNew.getEntry(j) + r1) 
						- mat.getEntry(j, i) * r2 * (estNew.getEntry(j) - estNew.getEntry(i) + r2);
				hessian.setEntry(i, i, valueii);
			}
		}
		hessian.walkInRowOrder(new RealMatrixChangingVisitor() {
			public double end() {
				return 0;
			}
			public void start(int arg0, int arg1, int arg2, int arg3, int arg4,
					int arg5) {			
			}
			public double visit(int row, int column, double value) {
				return (-1)*value;
			}
		});
		return hessian.getSubMatrix(1, actualLength - 1, 1, actualLength - 1);
	}
}