package net.andrewmao.math;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.StatisticalMultivariateSummary;
import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

public final class MeanOnlyMVS implements StatisticalMultivariateSummary { 
	
	private StorelessUnivariateStatistic[] meanImpl;
	private int k;
	
	public MeanOnlyMVS(int dimension) {
		k = dimension;
		
		meanImpl    = new StorelessUnivariateStatistic[k];

        for (int i = 0; i < k; ++i) meanImpl[i] = new Mean();	        
	}

    public void addValue(double[] value) {	    	        
    	for (int i = 0; i < k; ++i) meanImpl[i].increment(value[i]);	    	
    }
    
	@Override
	public int getDimension() {
		return k;
	}

	@Override
	public double[] getMean() {
        double[] results = new double[meanImpl.length];
        for (int i = 0; i < results.length; ++i) {
            results[i] = meanImpl[i].getResult();
        }
        return results;
	}

	@Override
	public RealMatrix getCovariance() {
		throw new RuntimeException("not implemented");
	}

	@Override
	public double[] getStandardDeviation() {
		throw new RuntimeException("not implemented");
	}

	@Override
	public double[] getMax() {
		throw new RuntimeException("not implemented");
	}

	@Override
	public double[] getMin() {
		throw new RuntimeException("not implemented");
	}

	@Override
	public long getN() {
		throw new RuntimeException("not implemented");
	}

	@Override
	public double[] getGeometricMean() {
		throw new RuntimeException("not implemented");
	}

	@Override
	public double[] getSum() {
		throw new RuntimeException("not implemented");
	}

	@Override
	public double[] getSumSq() {
		throw new RuntimeException("not implemented");
	}

	@Override
	public double[] getSumLog() {
		throw new RuntimeException("not implemented");
	}
}
