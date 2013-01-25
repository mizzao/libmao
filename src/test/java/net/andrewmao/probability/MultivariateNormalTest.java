package net.andrewmao.probability;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MultivariateNormalTest {

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCDF() {
		int n = 4;
		
		RealVector mean = new ArrayRealVector(n, 0);
		RealMatrix sigma = new Array2DRowRealMatrix(n, n);
		
		sigma.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
			@Override
			public double visit(int row, int column, double value) {				
				return row == column ? 1 : 0;
			}			
		});
		
		double[] lower = new double[n];
		
		double[] upper = new double[n];
		for( int i = 0; i < upper.length; i++ )
			upper[i] = Double.POSITIVE_INFINITY;
		
		double value = MultivariateNormal.cdf(mean, sigma, lower, upper);
		
		System.out.println("Obtained cdf:");
		System.out.println(value);
		
		assertEquals(Math.pow(0.5, 4), value, MultivariateNormal.cdf_default_abseps.getValue());
	}
	
	@Test
	public void testExpectation() {
		int n = 4;		
		
		RealVector mean = new ArrayRealVector(n, 0);
		RealMatrix sigma = new Array2DRowRealMatrix(n, n);
		
		sigma.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
			@Override
			public double visit(int row, int column, double value) {				
				return row == column ? 1 : 0;
			}			
		});
		
		double[] lower = new double[n];
		
		double[] upper = new double[n];
		for( int i = 0; i < upper.length; i++ )
			upper[i] = Double.POSITIVE_INFINITY;
		
		double[] values = MultivariateNormal.exp(mean, sigma, lower, upper);
		
		System.out.println("Obtained expected values:");		
		System.out.println(Arrays.toString(values));		
		
		for( double d : values )
			assertEquals(Math.sqrt(2.0/Math.PI), d, MultivariateNormal.cdf_default_releps.getValue());		
	}

	@Test
	public void testExpectationScaling() {
		// Giving wrong values after scaling! :D
		
		int n = 4;
		final double scale = 2.0d;
		
		RealVector mean = new ArrayRealVector(n, 0);
		RealMatrix sigma = new Array2DRowRealMatrix(n, n);
		
		sigma.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
			@Override
			public double visit(int row, int column, double value) {				
				return row == column ? scale * scale : 0;
			}
		});
		
		double[] lower = new double[n];
		
		double[] upper = new double[n];
		for( int i = 0; i < upper.length; i++ )
			upper[i] = Double.POSITIVE_INFINITY;
		
		double[] values = MultivariateNormal.exp(mean, sigma, lower, upper);
		
		System.out.println("Obtained expected values:");		
		System.out.println(Arrays.toString(values));		
		
		for( double d : values )
			assertEquals(scale * Math.sqrt(2.0/Math.PI), d, MultivariateNormal.cdf_default_releps.getValue() * scale);		
	}
	
	@Test
	public void testExpectationScaleBounds() {
		// Giving wrong values after scaling! :D
		
		int n = 4;
		final double scale = 2.0d;
		final double limit = 1.96;
		
		RealVector mean = new ArrayRealVector(n, 0);
		RealMatrix sigma = new Array2DRowRealMatrix(n, n);
		
		sigma.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
			@Override
			public double visit(int row, int column, double value) {				
				return row == column ? 1 : 0;
			}
		});
		
		double[] lower = new double[n];		
		double[] upper = new double[n];
		
		for( int i = 0; i < upper.length; i++ ) {
			lower[i] = 0;
			upper[i] = limit;
		}
		
		double[] values = MultivariateNormal.exp(mean, sigma, lower, upper);				
		
		System.out.println("Obtained expected values:");		
		System.out.println(Arrays.toString(values));		
		
		// Rescale sigma and bounds, and test
		sigma.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
			@Override
			public double visit(int row, int column, double value) {				
				return row == column ? value * scale * scale : 0;
			}
		});
		
		for( int i = 0; i < upper.length; i++ ) {
			lower[i] = lower[i] * scale;
			upper[i] = upper[i] * scale;
		}
		
		double[] values2 = MultivariateNormal.exp(mean, sigma, lower, upper);				
		
		System.out.println("Obtained expected values after scaling:");		
		System.out.println(Arrays.toString(values2));
				
		for( int i = 0; i < values.length; i++ ) {
			assertEquals(values[i], values2[i] / scale, MultivariateNormal.cdf_default_releps.getValue());
		}
	}
	
	@Test
	public void testExpectationInf() {
		int n = 4;		
		
		RealVector mean = new ArrayRealVector(n, 0);
		RealMatrix sigma = new Array2DRowRealMatrix(n, n);
		
		sigma.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
			@Override
			public double visit(int row, int column, double value) {				
				return row == column ? 1 : 0;
			}			
		});
		
		double[] lower = new double[n];		
		double[] upper = new double[n];
		
		for( int i = 0; i < upper.length; i++ ) {
			lower[i] = Double.NEGATIVE_INFINITY;
			upper[i] = Double.POSITIVE_INFINITY;
		}
			
		
		double[] values = MultivariateNormal.exp(mean, sigma, lower, upper);
		
		System.out.println("Obtained expected values:");		
		System.out.println(Arrays.toString(values));		
		
		for( double d : values )
			assertEquals(0, d, MultivariateNormal.cdf_default_releps.getValue());		
	}

}
