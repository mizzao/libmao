package net.andrewmao.models.games;

import java.text.NumberFormat;
import java.util.*;

import org.apache.commons.lang.NotImplementedException;

import be.ac.ulg.montefiore.run.jahmm.Opdf;

/**
 * Represents a set of distributions over actions corresponding to a set of signals,
 * also known as a game-theoretic strategy. 
 * 
 * @author mao
 *
 * @param <S>
 * @param <A>
 */
public class OpdfStrategy<S extends Enum<S>, A extends Enum<A>> 
implements Opdf<SignalActionObservation<S,A>> {
	
	private static final long serialVersionUID = 1L;

	final List<S> signals;
	final List<A> actions;
	
	final double[][] actionProbs;
	
	public OpdfStrategy(Class<S> signalClass, Class<A> actionClass, double[][] probs) {
		
		Set<S> sigs = EnumSet.allOf(signalClass);
		signals = new ArrayList<S>(sigs.size());
		for( S s : sigs )
			signals.set(s.ordinal(), s);
		
		Set<A> acts = EnumSet.allOf(actionClass);
		actions = new ArrayList<A>(acts.size());
		for( A a : acts )
			actions.set(a.ordinal(), a);
		
		this.actionProbs = probs;
	}
	
	@Override
	public double probability(SignalActionObservation<S,A> o) {				
		// pick the probability out of the actions for the given signal
		return actionProbs[o.signal.ordinal()][o.action.ordinal()];
	}

	@Override
	public SignalActionObservation<S,A> generate() {
		// Implementing this requires a signal distribution, which we aren't sure we need yet.
		throw new NotImplementedException();
	}

	@Override
	public void fit(SignalActionObservation<S,A>... oa) {
		fit(Arrays.asList(oa));		
	}

	@Override
	public void fit(Collection<? extends SignalActionObservation<S,A>> co) {
		double[] weights = new double[co.size()];
		Arrays.fill(weights, 1. / co.size());
		
		fit(co, weights);
	}

	@Override
	public void fit(SignalActionObservation<S,A>[] o, double[] weights) {
		fit(Arrays.asList(o), weights);		
	}

	@Override
	public void fit(Collection<? extends SignalActionObservation<S,A>> co, double[] weights) {
		if (co.isEmpty() || co.size() != weights.length)
			throw new IllegalArgumentException();
		
		for( double[] probs: actionProbs )
			Arrays.fill(probs, 0d);
		
		int i = 0;
		for( SignalActionObservation<S,A> sa: co ) {
			actionProbs[sa.signal.ordinal()][sa.action.ordinal()] += weights[i++];
		}
		
		// Re-normalize weights across each signal
		for( double[] probs: actionProbs ) {
			double sum = 0;			
			for( int j = 0; j < probs.length; j++ )
				sum += probs[j];
			
			for( int j = 0; j < probs.length; j++ )
				probs[j] /= sum;				
		}		
	}

	public String toString() {
		return toString(NumberFormat.getInstance());
	}
	
	@Override
	public String toString(NumberFormat numberFormat) {
		StringBuilder sb = new StringBuilder();
		
		for( A a : actions ) {
			sb.append("\t").append(a);
		}
		
		sb.append("\n");
		
		for( S s : signals ) {
			sb.append(s);
			for( A a : actions )
				sb.append("\t").append(actionProbs[s.ordinal()][a.ordinal()]);
		
			sb.append("\n");
		}		
		
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public OpdfStrategy<S,A> clone()
	{
		try {
			OpdfStrategy<S,A> opdf = (OpdfStrategy<S,A>) super.clone();
			
//			opdf.signals = new ArrayList<S>(signals);
//			opdf.actions = new ArrayList<A>(actions);
//			opdf.actionProbs = actionProbs.clone();
			
			return opdf;
		} catch(CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
	}

}
