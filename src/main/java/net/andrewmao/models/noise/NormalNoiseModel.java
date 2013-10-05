package net.andrewmao.models.noise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import net.andrewmao.math.RandomGeneration;
import net.andrewmao.probability.NormalDist;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

/**
 * Generates rankings with pairwise noise according to the Thurstone model,
 * using a standard normal distribution
 * 
 * @author mao
 *
 */
public class NormalNoiseModel<T> extends RandomUtilityModel<T> {
	
	private final double[] sds;
	private NormalLogLikelihood ll = null;
	
	public NormalNoiseModel(List<T> ordering, MeanVarParams params) {
		super(ordering, params.mean);
		
		sds = new double[params.variance.length];
		for( int j = 0; j < sds.length; j++ )
			sds[j] = Math.sqrt(params.variance[j]);
	}
	
	public NormalNoiseModel(List<T> candidates, double[] strengths, double[] sds) {
		super(candidates, strengths);
		
		this.sds = sds;
	}
	
	public NormalNoiseModel(List<T> ordering, double[] strengths, double stdev) {
		super(ordering, strengths);
		
		sds = new double[candidates.size()];		
		for( int j = 0; j < candidates.size(); j++ ) 
			sds[j] = stdev;
	}

	/**
	 * Initialized a fixed-variance probit model with the same difference between adjacent candidates
	 * After scaling, equivalent to Thurstone model.
	 * 
	 * @param stuffList
	 * @param random
	 * @param strDiff
	 * @param stdev
	 */
	public NormalNoiseModel(List<T> candidates, double adjStrDiff, double stdev) {
		super(candidates, adjStrDiff);
		
		sds = new double[candidates.size()];		
		for( int j = 0; j < candidates.size(); j++ ) 
			sds[j] = stdev;
	}

	@Override
	public String toParamString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(candidates.toString()).append("\n");
		sb.append(Arrays.toString(super.strParams)).append("\n");
		sb.append(Arrays.toString(sds));
		
		return sb.toString();	
	}

	@Override
	public double[] sampleUtilities(Random rnd) {
		return RandomGeneration.gaussianArray(strParams, sds, rnd);
	}
	
	public double[] getSigmas() {
		return sds;
	}

	@Override
	public double marginalProbability(T winner, T loser) {
		int w = candidates.indexOf(winner);
		int l = candidates.indexOf(loser);
				
		double s_w = sds[w], s_l = sds[l];				
		return NormalDist.cdf01((strParams[w] - strParams[l])/Math.sqrt(s_w*s_w + s_l*s_l));
	}

	@Override
	public double logLikelihood(PreferenceProfile<T> profile) {		
		if( ll == null ) {
			RealVector mean = new ArrayRealVector(super.strParams);
			RealVector variance = new ArrayRealVector(sds).mapToSelf(
					new UnivariateFunction() {				
						public double value(double x) {return x * x;}
					});
			ll = new NormalLogLikelihood(mean, variance); 
		}
		
		return ll.logLikelihood(profile, candidates);		
	}
	
	static String splitRegex = "[\\[\\] ,]+";

	public static NormalNoiseModel<?> parseParams(String params) {
		try(Scanner sc = new Scanner(params)) {
			
			String itemStr = sc.nextLine();
			String meanStr = sc.nextLine();
			String sdStr = sc.nextLine();
			
			String[] items = itemStr.split(splitRegex);
			String[] means = meanStr.split(splitRegex);
			String[] sds = sdStr.split(splitRegex);
			
			int m = items.length - 1;
			List<String> objs = new ArrayList<String>(m);
			double[] mus = new double[m];
			double[] sigmas = new double[m];
			for( int i = 0; i < m; i++ ) {
				objs.add(items[i+1]);
				mus[i] = Double.parseDouble(means[i+1]);
				sigmas[i] = Double.parseDouble(sds[i+1]);
			}
			
			return new NormalNoiseModel<String>(objs, mus, sigmas);
		}
	}

}
