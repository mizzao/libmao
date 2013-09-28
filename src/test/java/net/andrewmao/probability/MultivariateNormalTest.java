package net.andrewmao.probability;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import net.andrewmao.math.RandomSelection;
import net.andrewmao.models.discretechoice.OrderedNormalEM;
import net.andrewmao.models.discretechoice.OrderedNormalEM.MVNParams;
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
	static final double convergenceTol = 0.1;
	static Random rnd = new Random();
	
	int n = 4;
	RealVector mean4 = new ArrayRealVector(n, 0);
	RealMatrix sigmaI4 = new Array2DRowRealMatrix(n, n);
	
	double[] lower0 = new double[n];	
	double[] lowerInf = new double[n];
	double[] upperInf = new double[n];	
	
	public MultivariateNormalTest() {
		sigmaI4.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
			@Override
			public double visit(int row, int column, double value) {				
				return row == column ? 1 : 0;
			}			
		});
		
		for( int i = 0; i < lowerInf.length; i++ )
			lowerInf[i] = Double.NEGATIVE_INFINITY;					
		
		for( int i = 0; i < upperInf.length; i++ )
			upperInf[i] = Double.POSITIVE_INFINITY;
	}
	
	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCDF() {		
		double value = MultivariateNormal.cdf(mean4, sigmaI4, lower0, upperInf).cdf;
		
		System.out.println("Obtained cdf:");
		System.out.println(value);
		
		assertEquals(Math.pow(0.5, 4), value, MultivariateNormal.cdf_default_abseps.getValue());
	}
	
	@Test
	public void testCDFNan() {
		// These parameters caused the code to return a NaN
		double[] mean = new double[] {
				-0.16014433764135572, 0.23296920873087068, -1.0831027264270603, 0.8340188690166026, -0.24358107061636136, 0.9365121582217176, -0.7711081639504794, 1.2578460898256763, -0.8879043596823241
		};	
		
		double[][] sigma = new double[][] {
				new double[] { 3.7597841747150964, -1.2926666840840324, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
				new double[] {-1.2926666840840324, 1.867208915797311, -0.5745422317132788, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
				new double[] {0.0, -0.5745422317132788, 1.5586282095674566, -0.9840859778541778, 0.0, 0.0, 0.0, 0.0, 0.0},
				new double[] {0.0, 0.0, -0.9840859778541778, 2.397939732320359, -1.4138537544661813, 0.0, 0.0, 0.0, 0.0},
				new double[] {0.0, 0.0, 0.0, -1.4138537544661813, 2.076517167771443, -0.6626634133052616, 0.0, 0.0, 0.0},
				new double[] {0.0, 0.0, 0.0, 0.0, -0.6626634133052616, 1.8950077530026093, -1.2323443396973477, 0.0, 0.0},
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, -1.2323443396973477, 2.232344339697348, -1.0, 0.0},
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 2.00110835898604, -1.00110835898604},
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.00110835898604, 1.9730874712358166}										
		};
		
		double[] lower = new double[] {
				0.08259050655133376, -0.17049123959278323, 0.8675577283898703, -0.5385880887445085, 0.16903466295523986, -0.6803116755153749, 0.5161010093364202, -0.8891851498427454, 0.6321105211138612
		};
		
		double[] upper = new double[lower.length];
		Arrays.fill(upper, Double.POSITIVE_INFINITY);
		
		RealVector meanV = new ArrayRealVector(mean);
		RealMatrix sigmaM = new Array2DRowRealMatrix(sigma);
		
		CDFResult result = MultivariateNormal.cdf(meanV, sigmaM, lower, upper);
				
		assertFalse(Double.isNaN(result.cdf));
		System.out.println("NaN bug result: " + result.cdf);
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
		double expected = Math.sqrt(2.0/Math.PI);
		double[] values = MultivariateNormal.exp(mean4, sigmaI4, lower0, upperInf).expValues;
		
		System.out.println("Obtained expected values:");		
		System.out.println(Arrays.toString(values));						
		
		for( double d : values )
			assertEquals(expected, d, MultivariateNormal.exp_default_releps.getValue());		
		
		values = MultivariateNormal.eX2(mean4, sigmaI4, lower0, upperInf).expValues;
		
		System.out.println("Obtained expected values from 2nd order:");		
		System.out.println(Arrays.toString(values));						
		
		for( double d : values )
			assertEquals(expected, d, MultivariateNormal.exp_default_releps.getValue());		
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
			
			MVNParams params = OrderedNormalEM.getTransformedParams(mean, var, ranking);
			ExpResult result = MultivariateNormal.exp(
					params.mu, params.sigma, params.lower, params.upper,
					OrderedNormalEM.EM_MAXPTS_MULTIPLIER, null, null);			 
			
			if( !result.converged ) convergeFail++;			
		}
		
		double failRatio = 1.0 * convergeFail / trials;
		System.out.println("Expectation error failed %: " + failRatio);
				
		assertTrue(failRatio < convergenceTol);
	}
	
	@Test
	public void testExpectationScaling() {
		// Giving wrong values after scaling! :D		
		final double scale = 2.0d;		
		RealMatrix sigma = new Array2DRowRealMatrix(n, n);
		
		sigma.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
			@Override
			public double visit(int row, int column, double value) {				
				return row == column ? scale * scale : 0;
			}
		});
		
		double[] values = MultivariateNormal.exp(mean4, sigma, lower0, upperInf).expValues;
		
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
					
		double[] upper196 = new double[n];
		
		for( int i = 0; i < upper196.length; i++ ) {			
			upper196[i] = limit;
		}
		
		double[] values = MultivariateNormal.exp(mean4, sigmaI4, lower0, upper196).expValues;				
		
		System.out.println("Obtained expected values:");		
		System.out.println(Arrays.toString(values));		
		
		// Rescale sigma and bounds, and test
		RealMatrix sigma = sigmaI4.copy();
		sigma.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
			@Override
			public double visit(int row, int column, double value) {				
				return row == column ? value * scale * scale : 0;
			}
		});
		
		double[] lower = lower0.clone();
		double[] upperScaled = upper196.clone();
		
		for( int i = 0; i < upper196.length; i++ ) {
			lower[i] = lower[i] * scale;
			upperScaled[i] = upperScaled[i] * scale;
		}
		
		double[] values2 = MultivariateNormal.exp(mean4, sigma, lower, upperScaled).expValues;				
		
		System.out.println("Obtained expected values after scaling:");		
		System.out.println(Arrays.toString(values2));
				
		for( int i = 0; i < values.length; i++ ) {
			assertEquals(values[i], values2[i] / scale, MultivariateNormal.exp_default_releps.getValue());
		}
	}
	
	@Test
	public void testExpectationInf() {							
		double[] values = MultivariateNormal.exp(mean4, sigmaI4, lowerInf, upperInf).expValues;
		
		System.out.println("Obtained expected values:");		
		System.out.println(Arrays.toString(values));		
		
		for( double d : values )
			assertEquals(0, d, MultivariateNormal.exp_default_releps.getValue());		
	}

	@Test
	public void testEX2Octant() {		
		double[] values = MultivariateNormal.eX2(mean4, sigmaI4, lower0, upperInf).eX2Values;
		
		System.out.println("Obtained ex2 values:");		
		System.out.println(Arrays.toString(values));		
		
		double expected = 1d; // Mean of chi-square distribution with k=1
		
		for( double d : values )
			assertEquals(expected, d, MultivariateNormal.exp_default_releps.getValue());		
	}
	
	@Test
	public void testEX2Inf() {		
		double[] values = MultivariateNormal.eX2(mean4, sigmaI4, lowerInf, upperInf).eX2Values;
		
		System.out.println("Obtained ex2 values:");		
		System.out.println(Arrays.toString(values));		
		
		double expected = 1d; // Mean of chi-square distribution with k=1
		
		for( double d : values )
			assertEquals(expected, d, MultivariateNormal.exp_default_releps.getValue());		
	}
	
	@Test
	public void testEX2Error() {
		fail("Not implemented");
	}
	
	@Test
	public void testEX2Scaling() {
		// Giving wrong values after scaling! :D		
		final double scale = 2.0d;		
		RealMatrix sigma = new Array2DRowRealMatrix(n, n);
		
		sigma.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
			@Override
			public double visit(int row, int column, double value) {				
				return row == column ? scale * scale : 0;
			}
		});
		
		double[] values = MultivariateNormal.eX2(mean4, sigma, lower0, upperInf).eX2Values;
		
		System.out.println("Obtained ex2 scaled values:");		
		System.out.println(Arrays.toString(values));		
		
		double expected = scale * scale; // E[Y^2] = a^2E[X^2] for Y = aX
		
		for( double d : values )
			assertEquals(expected, d, MultivariateNormal.exp_default_releps.getValue() * scale * scale);		
	}
	
	/*
	 * Make sure the dodgy error calculation for the second moment is correct
	 */
	@Test
	public void testEX2ScaleBounds() {
		fail("Not implemented");
	}
}
