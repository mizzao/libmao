package net.andrewmao.models.discretechoice;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.analysis.function.Abs;
import org.apache.commons.math3.analysis.function.Sqrt;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.primitives.Ints;

import net.andrewmao.models.discretechoice.NormalGibbsSampler.NormalMoments;
import net.andrewmao.models.noise.NormalLogLikelihood;
import net.andrewmao.models.noise.NormalNoiseModel;
import net.andrewmao.socialchoice.rules.PreferenceProfile;
import net.andrewmao.stat.MultivariateMean;

/**
 * This is a general implementation of the probit model as described at
 * https://wiki.ece.cmu.edu/ddl/index.php/Introduction_to_random_utility_discrete_choice_models
 * 
 * @author mao
 *
 * @param <T>
 */
public class OrderedNormalMCEM extends MCEMModel<NormalMoments, NormalNoiseModel<?>> {

	final boolean floatVariance;
	
	MultivariateMean m1Stats;
	MultivariateMean m2Stats;
	
	RealVector delta, variance;
	NormalLogLikelihood ll;
	
	List<int[]> rankings;
	int numItems;	

	/**
	 * Created an ordered normal model using MCEM. A fixed variance is set to 1.
	 * 
	 * @param floatVariance whether the variance should be allowed to change during EM.
	 */
	public OrderedNormalMCEM(boolean floatVariance, int maxIters, double abseps, double releps) {
		super(maxIters, abseps, releps);
		this.floatVariance = floatVariance;
	}
	
	@Override
	protected void initialize(List<int[]> rankings, int m) {
		this.rankings = rankings;
		this.numItems = m;
		
		m1Stats = new MultivariateMean(m);				
		delta = new ArrayRealVector(start);
		
		if( floatVariance ) {
			m2Stats = new MultivariateMean(m);
			
			double[] randomVars = new NormalDistribution().sample(m);
			variance = new ArrayRealVector(randomVars).mapToSelf(new Abs()).mapAddToSelf(1);	
		}
		else {
			variance = new ArrayRealVector(m, 1.0d);
		}		
		
		ll = new NormalLogLikelihood(delta, variance);
	}

	@Override
	protected void eStep(int i) {
		/*
		 * E-step: parallelized Gibbs sampling			
		 */
		
		// TODO: where this number come from and why it depends on # iterations?
		int samples = 2000+300*i;
				
		m1Stats.clear();
		if( floatVariance ) m2Stats.clear();
		
		Multiset<List<Integer>> counts = HashMultiset.create();			
		for( int[] ranking : rankings )
			counts.add(Ints.asList(ranking));	
		
		for( Entry<List<Integer>> e : counts.entrySet() ) {
			int[] ranking = Ints.toArray(e.getElement());
			// TODO: make a separate gibbs sampler when we don't need the variance
			super.addJob(new NormalGibbsSampler(delta, variance, ranking, samples, e.getCount()));								
		}

	}

	@Override
	protected void addData(NormalMoments data) {
		for( int i = 0; i < data.weight; i++ ) {
			m1Stats.addValue(data.m1);
			if( floatVariance ) m2Stats.addValue(data.m2);	
		}
	}
	
	@Override
	protected void mStep() {
		/*
		 * M-step: re-compute parameters
		 */
		double[] eM1 = m1Stats.getMean();
		double[] eM2 = null;
		if( floatVariance) eM2 = m2Stats.getMean();
		
		for( int i = 0; i < eM1.length; i++ ) {
			double m = eM1[i];
			delta.setEntry(i, eM1[i]);			
			
			if( floatVariance ) variance.setEntry(i, eM2[i] - m*m);
		}
					
		/* adjust the mean and variance values to prevent drift:
		 * first subtract means so that first value is 0
		 * then scale variance to 1
		 */
				
		// Dunno what hossein was thinking with this, doesn't seem to work well
//		delta.setEntry(0, 1); 
//		variance.setEntry(0, 1);
		
		// Testing to see if parameters converge
//		variance.set(1);		
		
		// Adjust all variables so that first var is 1 		 
		if( floatVariance ) {
			double var = variance.getEntry(0);
			double sd = Math.sqrt(var);

			variance.mapDivideToSelf(var);
			delta.mapDivideToSelf(sd);
		}
		
		// Re-center means - first mean is 0
		delta.mapSubtractToSelf(delta.getEntry(0));
		
//		System.out.println(delta);
//		System.out.println(variance);
				
	}
	

	@Override
	protected double[] getCurrentParameters() {
		return delta.toArray();
	}

	public double getLogLikelihood() {		
		return ll.logLikelihood(rankings);
	}	

	@Override
	public <T> NormalNoiseModel<T> fitModel(PreferenceProfile<T> profile) {
		List<T> ordering = Arrays.asList(profile.getSortedCandidates());
		List<int[]> rankings = profile.getIndices(ordering);
		
		double[] strParams = getParameters(rankings, ordering.size());
		double[] sds = variance.map(new Sqrt()).toArray();
		
		return new NormalNoiseModel<T>(ordering, new Random(), strParams, sds);		
	}

}
