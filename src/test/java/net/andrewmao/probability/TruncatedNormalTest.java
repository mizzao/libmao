package net.andrewmao.probability;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TruncatedNormalTest {

	int samples = 10000;
	double stderr = 1 / Math.sqrt(samples);
	
	double almost_exact = 1e-10;
	
	SummaryStatistics stats = new SummaryStatistics();
	Class<? extends TruncatedNormal> tnClass;
	
	@Parameters(name="{0}")
	public static Collection<Object[]> tnImpls() {
		return Arrays.asList(
					new Object[] { TruncatedNormal.class },
					new Object[] { TruncatedNormalQuick.class }
				);
	}

	public TruncatedNormalTest(Class<? extends TruncatedNormal> tnClass) {
		this.tnClass = tnClass;
	}
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
		stats.clear();
	}
	
	TruncatedNormal getTnImpl(double mean, double sd, double lb, double ub) {
		try {
			return tnClass.getConstructor(double.class, double.class, double.class, double.class)
					.newInstance(mean, sd, lb, ub);
		} catch(Exception e) {
			fail();
			return null;
		}
	}
	
	@Test
	public void testMethods() {
		TruncatedNormal tn = getTnImpl(0, 1, -2, 2);
		
		assertEquals(1.0, tn.probability(-2, 2), almost_exact);
		
		assertEquals(0.5, tn.cumulativeProbability(0), almost_exact);
		assertEquals(0.5, tn.probability(0, 2), almost_exact);
		
		assertEquals(0, tn.inverseCumulativeProbability(0.5), almost_exact);
		assertEquals(-2, tn.inverseCumulativeProbability(0), almost_exact);
		assertEquals(2, tn.inverseCumulativeProbability(1), almost_exact);
	}
	
	@Test
	public void testDensity() {
		final TruncatedNormal tn = getTnImpl(0, 1, -2, 2);
		IterativeLegendreGaussIntegrator intg = new IterativeLegendreGaussIntegrator(5, 1e-7, 1e-11);
		
		UnivariateFunction density = new UnivariateFunction() {
			@Override
			public double value(double x) {				
				return tn.density(x);
			}
		};
		
		assertEquals(1, intg.integrate(1000, density, -2, 2), almost_exact);		
	}
	
	@Test
	public void testCenteredMean() {		
		TruncatedNormal tn = getTnImpl(0, 1, -2, 2);		
				
		for( int i = 0; i < samples; i++ )
			stats.addValue(tn.sample());
		
		assertEquals(0, stats.getMean(), 2*stderr);
		assertEquals(tn.getNumericalMean(), stats.getMean(), 2*stderr);
		
		assertEquals(Math.sqrt(tn.getNumericalVariance()), stats.getStandardDeviation(), 2*stderr);		
	}
	
	@Test
	public void testPositiveMean() {		
		TruncatedNormal tn = getTnImpl(0, 1, 0, 4);		
						
		for( int i = 0; i < samples; i++ )
			stats.addValue(tn.sample());
				
		assertEquals(tn.getNumericalMean(), stats.getMean(), 2*stderr);
		
		assertEquals(Math.sqrt(tn.getNumericalVariance()), stats.getStandardDeviation(), 2*stderr);		
	}

}
