package net.andrewmao.models.randomutility;

import java.util.Arrays;

import org.apache.commons.math3.analysis.DifferentiableMultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixChangingVisitor;
import org.apache.commons.math3.linear.RealVector;

/**
 * Thurstone-Mosteller log likelihood with first parameter fixed to 0 
 * to avoid overparametrization
 * 
 * @author Mao
 *
 */
public class TMLogLikelihood implements DifferentiableMultivariateFunction {
	
	private NormalDistribution normdist = new NormalDistribution();
	
	private RealMatrix mat;
	
	public TMLogLikelihood(RealMatrix mat) {
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

		double l = 0;
		
		for (int i = 0; i < actualLength; i++) {
			for (int j = 0; j < actualLength; j++) {
				if (i == j)	continue;
				
				l -= mat.getEntry(i, j)	* Math.log(normdist
						.cumulativeProbability(pointNew.getEntry(i) - pointNew.getEntry(j)));
			}
		}		

		return l;
	}

	/**
	 * Compute the partial derivative of the log likelihood function
	 */
	public MultivariateFunction partialDerivative(final int i) {
		return new MultivariateFunction() {
			@Override
			public double value(double[] point)	throws IllegalArgumentException {
				
				// Add 0 to the front of the array
				int actualLength = point.length + 1;
				RealVector pointNew = new ArrayRealVector(new double[] { 0.0 }, point);								

				double value = 0;
				
				for (int j = 0; j < actualLength; j++) {
					double i_minus_j = pointNew.getEntry(i) - pointNew.getEntry(j);
					double j_minus_i = pointNew.getEntry(j) - pointNew.getEntry(i);
					
					value += mat.getEntry(i, j)
							* normdist.density(i_minus_j)
							/ normdist.cumulativeProbability(i_minus_j);
					value -= mat.getEntry(j, i)
							* normdist.density(j_minus_i)
							/ normdist.cumulativeProbability(j_minus_i);
				}
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
						double j_minus_i = pointNew.getEntry(j) - pointNew.getEntry(i);

						values[i] += mat.getEntry(i, j)
								* normdist.density(i_minus_j)
								/ normdist.cumulativeProbability(i_minus_j);
						values[i] -= mat.getEntry(j, i)
								* normdist.density(j_minus_i)
								/ normdist.cumulativeProbability(j_minus_i);

					}
					values[i] = (-1)* values[i];
				}
				
				double[] gradient = Arrays.copyOfRange(values, 1, actualLength);
				return gradient;
			}
		};
	}

	/**
	 * Compute the 2nd order derivatives of the log likelihood function
	 */
	public RealMatrix hessian(RealVector est) {
		int actualLength = est.getDimension() + 1;
		RealVector estNew = new ArrayRealVector(new double[] {0.0});
		estNew = estNew.append(est);
		
		RealMatrix hessian = new Array2DRowRealMatrix(actualLength, actualLength);
		for (int i = 0; i < actualLength; i++) {
			for (int j = 0; j < actualLength; j++) {
				if (i == j)	continue;
				
				double r1 = normdist.density(estNew.getEntry(i) - estNew.getEntry(j))
						   	/ normdist.cumulativeProbability(estNew.getEntry(i) - estNew.getEntry(j));
				double r2 = normdist.density(estNew.getEntry(j) - estNew.getEntry(i))
						   	/ normdist.cumulativeProbability(estNew.getEntry(j) - estNew.getEntry(i));
				
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