package net.andrewmao.probability;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import net.andrewmao.math.RandomSelection;
import net.andrewmao.models.discretechoice.OrderedNormalEM;
import net.andrewmao.models.noise.NormalLogLikelihood;
import net.andrewmao.models.noise.TestParameterGen;
import net.andrewmao.probability.MultivariateNormal.CDFResult;
import net.andrewmao.probability.MultivariateNormal.ExpResult;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MultivariateNormalTest {

	// The amount of time we tolerate not getting within the error
	static final double trials = 1000;
	static final double convergenceTol = 0.15;
	static Random rnd = new Random();
	
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
		
		double value = MultivariateNormal.cdf(mean, sigma, lower, upper).value;
		
		System.out.println("Obtained cdf:");
		System.out.println(value);
		
		assertEquals(Math.pow(0.5, 4), value, MultivariateNormal.cdf_default_abseps.getValue());
	}
		
	@Test 
	public void testCDFError() {
		// Make sure the default CDF settings are sufficient for a ranking of 4 with given accuracy		
		int m = 4;
		int[] ranking = new int[] {1, 2, 3, 4};
		
		int convergeFail = 0;
		
		for( int i = 0; i < trials; i++ ) {
			RealVector mean = TestParameterGen.randomMeanVector(m);
			RealVector var = TestParameterGen.randomVarVector(m);
			RandomSelection.shuffle(ranking, rnd);
			
			CDFResult result = NormalLogLikelihood.multivariateProb(mean, var, ranking);
			if( !result.converged ) {				
				convergeFail++;
			}
		}
		
		double failRatio = 1.0 * convergeFail / trials;
		System.out.println("CDF error failed %: " + failRatio);
				
		assertTrue(failRatio < convergenceTol);
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
		
		double[] values = MultivariateNormal.exp(mean, sigma, lower, upper).values;
		
		System.out.println("Obtained expected values:");		
		System.out.println(Arrays.toString(values));		
		
		for( double d : values )
			assertEquals(Math.sqrt(2.0/Math.PI), d, MultivariateNormal.exp_default_releps.getValue());		
	}

	@Test 
	public void testExpectationError() {
		// Make sure the default CDF settings are sufficient for a ranking of 4 with given accuracy		
		int m = 4;
		int[] ranking = new int[] {1, 2, 3, 4};
		
		int convergeFail = 0;
		
		for( int i = 0; i < trials; i++ ) {
			RealVector mean = TestParameterGen.randomMeanVector(m);
			RealVector var = TestParameterGen.randomVarVector(m);
			RandomSelection.shuffle(ranking, rnd);
			
			ExpResult result = OrderedNormalEM.multivariateExp(mean, var, ranking, 1, null);
			if( !result.converged ) {				
				convergeFail++;
			}
		}
		
		double failRatio = 1.0 * convergeFail / trials;
		System.out.println("Expectation error failed %: " + failRatio);
				
		assertTrue(failRatio < convergenceTol);
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
		
		double[] values = MultivariateNormal.exp(mean, sigma, lower, upper).values;
		
		System.out.println("Obtained expected values:");		
		System.out.println(Arrays.toString(values));		
		
		for( double d : values )
			assertEquals(scale * Math.sqrt(2.0/Math.PI), d, MultivariateNormal.exp_default_releps.getValue() * scale);		
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
		
		double[] values = MultivariateNormal.exp(mean, sigma, lower, upper).values;				
		
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
		
		double[] values2 = MultivariateNormal.exp(mean, sigma, lower, upper).values;				
		
		System.out.println("Obtained expected values after scaling:");		
		System.out.println(Arrays.toString(values2));
				
		for( int i = 0; i < values.length; i++ ) {
			assertEquals(values[i], values2[i] / scale, MultivariateNormal.exp_default_releps.getValue());
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
			
		
		double[] values = MultivariateNormal.exp(mean, sigma, lower, upper).values;
		
		System.out.println("Obtained expected values:");		
		System.out.println(Arrays.toString(values));		
		
		for( double d : values )
			assertEquals(0, d, MultivariateNormal.exp_default_releps.getValue());		
	}

}
