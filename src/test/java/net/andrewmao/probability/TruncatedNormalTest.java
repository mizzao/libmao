package net.andrewmao.probability;

import static org.junit.Assert.*;

import net.andrewmao.probability.TruncatedNormal;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TruncatedNormalTest {

	int samples = 10000;
	double stderr = 1 / Math.sqrt(samples);
	
	SummaryStatistics stats = new SummaryStatistics();
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		stats.clear();
	}

	@Test
	public void testMethods() {
		TruncatedNormal tn = new TruncatedNormal(0, 1, -2, 2);
		
		assertEquals(0.5, tn.cumulativeProbability(0), 1e-10);
		assertEquals(0.5, tn.probability(0, 2), 1e-10);
		
		assertEquals(0, tn.inverseCumulativeProbability(0.5), 1e-10);
		assertEquals(-2, tn.inverseCumulativeProbability(0), 1e-10);
		assertEquals(2, tn.inverseCumulativeProbability(1), 1e-10);
	}
	
	@Test
	public void testDensity() {
		final TruncatedNormal tn = new TruncatedNormal(0, 1, -2, 2);
		IterativeLegendreGaussIntegrator intg = new IterativeLegendreGaussIntegrator(5, 1e-7, 1e-11);
		
		UnivariateFunction density = new UnivariateFunction() {
			@Override
			public double value(double x) {				
				return tn.density(x);
			}			
		};
		
		assertEquals(1, intg.integrate(1000, density, -2, 2), 1e-10);		
	}
	
	@Test
	public void test() {		
		TruncatedNormal tn = new TruncatedNormal(0, 1, -2, 2);		
				
		for( int i = 0; i < samples; i++ )
			stats.addValue(tn.sample());
		
		assertEquals(0, stats.getMean(), 2*stderr);
		assertEquals(tn.getNumericalMean(), stats.getMean(), 2*stderr);
		
		assertEquals(Math.sqrt(tn.getNumericalVariance()), stats.getStandardDeviation(), 2*stderr);		
	}
	
	@Test
	public void test2() {		
		TruncatedNormal tn = new TruncatedNormal(0, 1, 0, 4);		
						
		for( int i = 0; i < samples; i++ )
			stats.addValue(tn.sample());
				
		assertEquals(tn.getNumericalMean(), stats.getMean(), 2*stderr);
		
		assertEquals(Math.sqrt(tn.getNumericalVariance()), stats.getStandardDeviation(), 2*stderr);		
	}

}
