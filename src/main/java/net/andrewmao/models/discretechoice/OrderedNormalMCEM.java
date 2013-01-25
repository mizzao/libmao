package net.andrewmao.models.discretechoice;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.analysis.function.Abs;
import org.apache.commons.math3.analysis.function.Sqrt;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

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

	MultivariateMean m1Stats;
	MultivariateMean m2Stats;
	
	RealVector delta, variance;
	NormalLogLikelihood ll;
	
	List<int[]> rankings;
	int numItems;	

	@Override
	protected void initialize(List<int[]> rankings, int m) {
		this.rankings = rankings;
		this.numItems = m;
		
		m1Stats = new MultivariateMean(m);
		m2Stats = new MultivariateMean(m);
		
		delta = new ArrayRealVector(start);		
		// Can either initialize variance randomly or fixed 
		double[] randomVars = new NormalDistribution().sample(m);
		variance = new ArrayRealVector(randomVars).mapToSelf(new Abs()).mapAddToSelf(1);
		
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
		m2Stats.clear();		
		
		for( int[] ranking : rankings ) {				
			super.addJob(new NormalGibbsSampler(delta, variance, ranking, samples));
		}		
	}

	@Override
	protected void addData(NormalMoments data) {		
		m1Stats.addValue(data.m1);
		m2Stats.addValue(data.m2);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void mStep() {
		/*
		 * M-step: re-compute parameters
		 */
		delta = new ArrayRealVector(m1Stats.getMean());		
		variance = new ArrayRealVector(m2Stats.getMean()).subtract(delta.ebeMultiply(delta));
					
		/* adjust the mean and variance values to prevent drift:
		 * first subtract means so that first value is 0
		 * TODO then scale variance to 1
		 */
				
		// Dunno what hossein was thinking with this, doesn't seem to work well
//		delta.setEntry(0, 1); 
//		variance.setEntry(0, 1);
		
		// Testing to see if parameters converge
//		variance.set(1);		
		
		/* The below adjusts all variables so that
		 * first mean is 0, first var is 1 
		 */
		double var = variance.getEntry(0);
		double sd = Math.sqrt(var);
		
		variance.mapDivideToSelf(var);
		delta.mapDivideToSelf(sd);		
		delta.mapSubtractToSelf(delta.getEntry(0));
		
//		System.out.println(delta);
//		System.out.println(variance);
		
		/* Right now we have to re-initialize the LL due to re-defining delta, variance
		 * TODO: reduce continued initialization of this
		 */
		ll = new NormalLogLikelihood(delta, variance);
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
