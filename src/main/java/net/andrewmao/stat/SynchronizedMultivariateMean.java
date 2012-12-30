package net.andrewmao.stat;

public class SynchronizedMultivariateMean extends MultivariateMean {

	public SynchronizedMultivariateMean(int dimension) {
		super(dimension);		
	}
		
	@Override
	public synchronized void clear() {		
		super.clear();
	}

	@Override
	public synchronized void addValue(int[] value) {
		super.addValue(value);
	}
	
	@Override
	public synchronized void addValue(double[] value) {
		super.addValue(value);
	}
	
	@Override
	public synchronized double[] getMean() {		
		return super.getMean();
	}	

}
