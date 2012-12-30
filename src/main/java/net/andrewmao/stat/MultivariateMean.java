package net.andrewmao.stat;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

public class MultivariateMean { 
	
	private Mean[] meanImpl;
	private final int k;
	
	public MultivariateMean(int dimension) {
		k = dimension;
		
		meanImpl    = new Mean[k];

        for (int i = 0; i < k; ++i) meanImpl[i] = new Mean();	        
	}

	public void clear() {
		for( Mean m : meanImpl ) m.clear();
	}

	public void addValue(int[] value) {
		for (int i = 0; i < k; ++i) meanImpl[i].increment(value[i]);
	}
	
    public void addValue(double[] value) {	    	        
    	for (int i = 0; i < k; ++i) meanImpl[i].increment(value[i]);	    	
    }
    	
	public int getDimension() {
		return k;
	}
	
	public double[] getMean() {
        double[] results = new double[meanImpl.length];
        for (int i = 0; i < results.length; ++i) {
            results[i] = meanImpl[i].getResult();
        }
        return results;
	}

}
