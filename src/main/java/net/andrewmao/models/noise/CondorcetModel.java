package net.andrewmao.models.noise;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.ArrayUtils;


import net.andrewmao.math.RandomSelection;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

/**
 * Generates preference profiles with Condorcet noise
 * @author mao
 *
 * @param <T>
 */
public class CondorcetModel<T> extends NoiseModel<T> {
		
	final double phi;
	
	public CondorcetModel(List<T> candidates, Random rnd, double prob) {
		super(candidates, rnd);
		
		if(prob > 1.0 || prob < 0.5) 
			throw new IllegalArgumentException("p must be in the range [0.5, 1]");
		
		this.phi = (1.0 - prob) / prob;
	}
	
	public String toString() {
		return candidates.toString() + " p=" + 1/(1+phi);
	}
	
	static double[] getWeights(int i, double phi) {
		// Draw integers from 0 .. i with probability p_ij as defined in Lu & Boutilier paper
		double[] wts = new double[i + 1];
		for( int j = 0; j <= i; j++ )
			wts[j] = Math.pow(phi, i - j);		
		return wts;
	}
	
	int[] getNewInsertionVector() {
		int[] insVec = new int[candidates.size()];
		
		for( int i = 0; i < insVec.length; i++ ) {
			double[] wts = getWeights(i, phi);
			
			insVec[i] = RandomSelection.selectRandomWeighted(wts, rnd);			
		}
		
		return insVec;
	}
	
	List<T> getInsertedList(int[] insVec) {
		// Insert candidates at the position specified by the insertion vector
		List<T> ranking = new ArrayList<T>();
		for( int j = 0; j < candidates.size(); j++ )
			ranking.add(insVec[j], candidates.get(j));
		
		return ranking;
	}

	@Override
	public PreferenceProfile<T> sampleProfile(int size) {
		T[][] profile = super.getProfileArray(size);
		
		for( int i = 0; i < size; i++ ) {
			int[] insVec = getNewInsertionVector();
			
			List<T> ranking = getInsertedList(insVec);
											
			ranking.toArray(profile[i]);
		}
		
		return new PreferenceProfile<T>(profile);
	}

	@Override
	public double logLikelihood(PreferenceProfile<T> profile) {
		return profileLogLikelihood(profile, candidates, phi);
	}

	public static <T> double profileLogLikelihood(
			PreferenceProfile<T> profile, List<T> trueRanking, double phi) {
		double ll = 0;

		for( T[] preference : profile.getProfile() ) {
			/*
			 * Compute probability as would occur by insertion
			 * First candidate has insertion probability 1
			 */
			
			for( int i = 1; i < trueRanking.size(); i++ ) {
				T alternative = trueRanking.get(i);
				
				int insertionIdx = ArrayUtils.indexOf(preference, alternative);
				int diff = 0;
				// Subtract 1 for everything that comes after i in the true ranking				
				for( int j = 0; j < insertionIdx; j++ ) {
					if( trueRanking.indexOf(preference[j]) > i ) diff++;			
				}				
				
				ll += Math.log(getInsertionProbs(i, phi)[insertionIdx - diff]);
			}			
		}		
		
		return ll;
	}
	
	private static double[] getInsertionProbs(int i, double phi) {
		double[] weights = getWeights(i, phi);
		
		double sum = 0;
		for( double d : weights ) sum += d;
		for( int j = 0; j < weights.length; j++ )
			weights[j] /= sum;
		
		return weights;
	}

}
