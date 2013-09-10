package net.andrewmao.models.games;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;

import be.ac.ulg.montefiore.run.jahmm.Opdf;

public class OpdfSignalAction implements Opdf<SignalActionObservation> {
	
	private static final long serialVersionUID = 1L;

	@Override
	public double probability(SignalActionObservation o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SignalActionObservation generate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fit(SignalActionObservation... oa) {
		fit(Arrays.asList(oa));		
	}

	@Override
	public void fit(Collection<? extends SignalActionObservation> co) {
		double[] weights = new double[co.size()];
		Arrays.fill(weights, 1. / co.size());
		
		fit(co, weights);
	}

	@Override
	public void fit(SignalActionObservation[] o, double[] weights) {
		fit(Arrays.asList(o), weights);		
	}

	@Override
	public void fit(Collection<? extends SignalActionObservation> co, double[] weights) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString(NumberFormat numberFormat) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public OpdfSignalAction clone()
	{
		try {
			return (OpdfSignalAction) super.clone();
		} catch(CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
	}

}
