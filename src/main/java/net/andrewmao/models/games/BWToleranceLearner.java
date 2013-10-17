package net.andrewmao.models.games;

import java.util.EnumSet;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.ForwardBackwardScaledCalculator;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.Observation;
import be.ac.ulg.montefiore.run.jahmm.ForwardBackwardCalculator.Computation;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchScaledLearner;

public class BWToleranceLearner extends BaumWelchScaledLearner {
	
	final int maxIters;
	final double abseps;
	final double releps;	
	
	public BWToleranceLearner() {
		this(50, 1e-3, 1e-6);
	}
	
	public BWToleranceLearner(int maxIters, double abseps, double releps) {
		this.maxIters = maxIters;
		this.abseps = abseps;
		this.releps = releps;
	}	

	@Override
	public <O extends Observation> Hmm<O>
	learn(Hmm<O> initialHmm, List<? extends List<? extends O>> sequences)
	{
		double logLk = Double.NEGATIVE_INFINITY;
				
		Hmm<O> hmm = initialHmm;		
		
		/* TODO This is a little inefficient because the iteration 
		 * runs forward-backward algorithm already, but it's quick so w/e 
		 */		
		
		for (int i = 0; i < maxIters; i++) {
			hmm = iterate(hmm, sequences);
			
			double newLogLk = computeLogLk(hmm, sequences);
			System.out.println(newLogLk);			
			
			double absDiff = newLogLk - logLk;				
			double relDiff = -absDiff / logLk;
			// Don't get tricked by the initial jump
			if( Double.isNaN(relDiff) ) relDiff = Double.POSITIVE_INFINITY; 							
			
			if ( absDiff < abseps || relDiff < releps ) break;
			
			logLk = newLogLk;
		}
		
		return hmm;
	}
	
	private static <O extends Observation> double 
	computeLogLk(Hmm<O> hmm, List<? extends List<? extends O>>sequences) {
		double logLk = 0;
		EnumSet<Computation> alphaOnly = EnumSet.of(Computation.ALPHA);
		
		for( List<? extends O> seq : sequences ) {
			ForwardBackwardScaledCalculator fbc = new ForwardBackwardScaledCalculator(seq, hmm, alphaOnly);
			logLk += fbc.lnProbability();
		}		
		
		return logLk;
	}
	
}
