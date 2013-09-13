package net.andrewmao.models.games;

import java.text.NumberFormat;
import java.util.*;

import net.andrewmao.math.RandomSelection;

import be.ac.ulg.montefiore.run.jahmm.Opdf;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;

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
implements Opdf<SigActObservation<S,A>> {
	
	private static final long serialVersionUID = 1L;

	final List<S> signals;
	final List<A> actions;
	
	final double[] signalPrior;
	final OpdfInteger signalDistribution;
	
	final double[][] actionProbs;
	
	Random rnd = new Random();
	
	public OpdfStrategy(Class<S> signalClass, Class<A> actionClass, 
			double[] signalPrior, double[][] probs) {
		
		Set<S> sigs = EnumSet.allOf(signalClass);
		signals = new ArrayList<S>(sigs);
		for( S s : sigs )
			signals.set(s.ordinal(), s);
		
		Set<A> acts = EnumSet.allOf(actionClass);
		actions = new ArrayList<A>(acts);
		for( A a : acts )
			actions.set(a.ordinal(), a);
		
		this.signalPrior = signalPrior;
		this.actionProbs = probs;
		
		if( signalPrior != null ) {
			// TODO make sure this takes the prior in the same order
			signalDistribution = new OpdfInteger(signalPrior);
		}
		else signalDistribution = null;
	}
	
	@Override
	public double probability(SigActObservation<S,A> o) {				
		// pick the probability out of the actions for the given signal
		return actionProbs[o.signal.ordinal()][o.action.ordinal()];
	}

	@Override
	public SigActObservation<S,A> generate() {
		if( signalPrior == null )
			throw new UnsupportedOperationException("Can't generate signals without prior");
		
		int sigIdx = signalDistribution.generate().value;
		S sig = signals.get(sigIdx);
		int actIdx = RandomSelection.selectRandomWeighted(actionProbs[sigIdx], rnd);
		A act = actions.get(actIdx);
		
		return new SigActObservation<S,A>(sig, act);
	}

	@Override
	public void fit(SigActObservation<S,A>... oa) {
		fit(Arrays.asList(oa));		
	}

	@Override
	public void fit(Collection<? extends SigActObservation<S,A>> co) {
		double[] weights = new double[co.size()];
		Arrays.fill(weights, 1. / co.size());
		
		fit(co, weights);
	}

	@Override
	public void fit(SigActObservation<S,A>[] o, double[] weights) {
		fit(Arrays.asList(o), weights);		
	}

	@Override
	public void fit(Collection<? extends SigActObservation<S,A>> co, double[] weights) {
		if (co.isEmpty() || co.size() != weights.length)
			throw new IllegalArgumentException();
		
		for( double[] probs: actionProbs )
			Arrays.fill(probs, 0d);
		
		int i = 0;
		for( SigActObservation<S,A> sa: co ) {
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
		
		// Initial line so stuff prints out evenly
		sb.append("\n");
		
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
