package net.andrewmao.models.discretechoice;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.analysis.function.Abs;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.primitives.Ints;

import net.andrewmao.models.noise.MeanVarParams;
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
public class OrderedNormalMCEM extends MCEMModel<NormalMoments, NormalNoiseModel<?>, MeanVarParams> {

	final boolean floatVariance;
	final int startingSamples;
	final int incrSamples;
	final Integer maxPtsScale;
	
	MultivariateMean m1Stats;
	MultivariateMean m2Stats;
	
	RealVector delta, variance;
	NormalLogLikelihood ll;
	volatile double lastLL;
	Multiset<List<Integer>> counts;
	
	List<int[]> rankings;
	int numItems;	

	/**
	 * Created an ordered normal model using MCEM. A fixed variance is set to 1.
	 * 
	 * @param floatVariance whether the variance should be allowed to change during EM.
	 */
	public OrderedNormalMCEM(boolean floatVariance, int maxIters, double abseps, double releps, Integer maxPtsScale, int startingSamples, int incrSamples) {
		super(maxIters, abseps, releps);		
		this.floatVariance = floatVariance;		
		this.startingSamples = startingSamples;
		this.incrSamples = incrSamples;
		this.maxPtsScale = maxPtsScale;
	}
	
	/**
	 * Default ordered normal MCEM model with 2000 starting samples and 300 add'l per iteration.
	 * 
	 * @param floatVariance
	 * @param maxIters
	 * @param abseps
	 * @param releps
	 */
	public OrderedNormalMCEM(boolean floatVariance, int maxIters, double abseps, double releps) {
		this(floatVariance, maxIters, abseps, releps, null, 2000, 300);
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
		
		if( maxPtsScale != null )
			ll = new NormalLogLikelihood(delta, variance, maxPtsScale, abseps, releps, EstimatorUtils.threadPool);
		else
			ll = new NormalLogLikelihood(delta, variance, EstimatorUtils.threadPool);
				
		counts = HashMultiset.create();			
		for( int[] ranking : rankings )
			counts.add(Ints.asList(ranking));
	}

	@Override
	protected void eStep(int i) {
		/*
		 * E-step: parallelized Gibbs sampling
		 * # Samples are increased as we get closer to true goal			
		 */	
		int samples = startingSamples + incrSamples*i;
				
		m1Stats.clear();
		if( floatVariance ) m2Stats.clear();	
		
		for( Entry<List<Integer>> e : counts.entrySet() ) {
			int[] ranking = Ints.toArray(e.getElement());
			int weight = e.getCount();
			
			super.addJob(new NormalGibbsSampler(delta, variance, ranking, samples, floatVariance, weight));							
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
		if( floatVariance ) eM2 = m2Stats.getMean();
		
		for( int i = 0; i < eM1.length; i++ ) {
			double m = eM1[i];
			delta.setEntry(i, m);			
			
			if( floatVariance ) variance.setEntry(i, eM2[i] - m*m);
		}
					
		/* 
		 * adjust the mean and variance values to prevent drift:
		 * first subtract means so that first value is 0
		 * then scale variance to 1
		 */	
		
		// Adjust all variables so that first var is 1 		 
		if( floatVariance ) {
			double var = variance.getEntry(0);			
			variance.mapDivideToSelf(var);
			delta.mapDivideToSelf(Math.sqrt(var));
		}
		
		// Re-center means - first mean is 0
		delta.mapSubtractToSelf(delta.getEntry(0));
		
//		System.out.println(delta);
//		System.out.println(variance);
				
	}	

	@Override
	protected MeanVarParams getCurrentParameters() {
		return new MeanVarParams(delta.toArray(), variance.toArray());
	}

	public double getLogLikelihood() {		
		// Don't modify any parameters as this can happen multi-threaded
		return lastLL = ll.logLikelihood(counts);
	}	

	@Override
	public <T> NormalNoiseModel<T> fitModelOrdinal(PreferenceProfile<T> profile) {		
		List<T> ordering = Arrays.asList(profile.getSortedCandidates());
		List<int[]> rankings = profile.getIndices(ordering);
		
		// Default initialization if setup not called
		if (this.start == null || this.start.length != ordering.size() )
			setup(new NormalDistribution().sample(ordering.size()));
		
		MeanVarParams params = getParameters(rankings, ordering.size());		
		
		NormalNoiseModel<T> nn = new NormalNoiseModel<T>(ordering, params);
		nn.setFittedLikelihood(lastLL);
		
		this.start = null; // reset the start point for next run 
		
		return nn;		
	}

}
