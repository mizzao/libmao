package net.andrewmao.models.randomutility;

import java.util.Arrays;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.DifferentiableMultivariateRealFunction;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.analysis.MultivariateVectorialFunction;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.MatrixIndexException;
import org.apache.commons.math.linear.MatrixVisitorException;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealMatrixChangingVisitor;
import org.apache.commons.math.linear.RealVector;

/**
 * Thurstone-Mosteller log likelihood with first parameter fixed to 0 
 * to avoid overparametrization
 * 
 * @author Mao
 *
 */
public class TMLogLikelihood implements DifferentiableMultivariateRealFunction {
	
	private NormalDistribution normdist = new NormalDistributionImpl();
	
	private RealMatrix mat;
	
	public TMLogLikelihood(RealMatrix mat) {
		this.mat = mat;
	}
	
	/**
	 * Calculate the log likelihood of matrix @mat given strength estimate @point
	 */
	public double value(final double[] point)
			throws FunctionEvaluationException, IllegalArgumentException {

		// Add 0 to the front of the array
		int actualLength = point.length + 1;
		RealVector pointNewVector = new ArrayRealVector(new double[] { 0.0 });
		pointNewVector = pointNewVector.append(point);
		double[] pointNew = pointNewVector.getData();

		double l = 0;
		try {
			for (int i = 0; i < actualLength; i++) {
				for (int j = 0; j < actualLength; j++) {
					if (i == j)
						continue;
					l -= mat.getEntry(i, j)	* Math.log(normdist
							.cumulativeProbability(pointNew[i] - pointNew[j]));
				}
			}
		} catch (MatrixIndexException e) {
			e.printStackTrace();
		} catch (MathException e) {
			e.printStackTrace();
		}

		return l;
	}

	/**
	 * Compute the partial derivative of the log likelihood function
	 */
	public MultivariateRealFunction partialDerivative(final int i) {
		return new MultivariateRealFunction() {
			public double value(double[] point)	
					throws FunctionEvaluationException,	IllegalArgumentException {
				
				// Add 0 to the front of the array
				int actualLength = point.length + 1;
				RealVector pointNewVector = new ArrayRealVector(new double[] {0.0});
				pointNewVector = pointNewVector.append(point);
				double[] pointNew = pointNewVector.getData();

				double value = 0;
				
				for (int j = 0; j < actualLength; j++) {
					try {
						value += mat.getEntry(i, j)
								* normdist.density(pointNew[i] - pointNew[j])
								/ normdist.cumulativeProbability(pointNew[i]- pointNew[j]);
						value -= mat.getEntry(j, i)
								* normdist.density(pointNew[j] - pointNew[i])
								/ normdist.cumulativeProbability(pointNew[j]- pointNew[i]);
					} catch (MatrixIndexException e) {
						e.printStackTrace();
					} catch (MathException e) {
						e.printStackTrace();
					}
				}
				return (-1)*value;
			}
		};
	}

	/**
	 * Compute the gradient of the log likelihood function
	 */
	public MultivariateVectorialFunction gradient() {
		return new MultivariateVectorialFunction() {
			public double[] value(double[] point) 
					throws FunctionEvaluationException, IllegalArgumentException {

				// Add 0 to the front of the array
				int actualLength = point.length + 1;
				RealVector pointNewVector = new ArrayRealVector(new double[] {0.0});
				pointNewVector = pointNewVector.append(point);
				double[] pointNew = pointNewVector.getData();

				
				double[] values = new double[actualLength];
				Arrays.fill(values, 0.0);
				
				for (int i = 0; i < actualLength; i++) {
					for (int j = 0; j < actualLength; j++) {
						if (i == j) continue;
						try {
							values[i] += mat.getEntry(i, j)
										* normdist.density(pointNew[i] - pointNew[j])
										/ normdist.cumulativeProbability(pointNew[i]- pointNew[j]);
							values[i] -= mat.getEntry(j, i)
										* normdist.density(pointNew[j] - pointNew[i])
										/ normdist.cumulativeProbability(pointNew[j]- pointNew[i]);

						} catch (MatrixIndexException e) {
							e.printStackTrace();
						} catch (MathException e) {
							e.printStackTrace();
						}
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
	public RealMatrix hessian(RealVector est) throws MathException {
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
			public double visit(int row, int column, double value)
					throws MatrixVisitorException {
				return (-1)*value;
			}
		});
		return hessian.getSubMatrix(1, actualLength - 1, 1, actualLength - 1);
	}
}