package net.andrewmao.socialchoice.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import net.andrewmao.math.RandomSelection;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

/**
 * Generates preference profiles with Condorcet noise
 * @author mao
 *
 * @param <T>
 */
public class CondorcetNoiseGenerator<T> extends PreferenceGenerator<T> {
	
	private double phi;		
	
	public CondorcetNoiseGenerator(List<T> candidates, Random rnd, double prob) {
		super(candidates, rnd);
		
		if(prob > 1.0 || prob < 0.5) 
			throw new IllegalArgumentException("p must be in the range [0.5, 1]");
		
		this.phi = (1.0 - prob) / prob;
	}
	
	double[] getWeights(int i) {
		// Draw integers from 0 .. i with probability p_ij as defined in Lu & Boutilier paper
		double[] wts = new double[i + 1];
		for( int j = 0; j <= i; j++ ) 
			wts[j] = Math.pow(phi, i - j);		
		return wts;
	}
	
	int[] getNewInsertionVector() {
		int[] insVec = new int[candidates.size()];
		
		for( int i = 0; i < insVec.length; i++ ) {
			double[] wts = getWeights(i);
			
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
	public PreferenceProfile<T> getRandomProfile(int size) {
		T[][] profile = super.getProfileArray(size);
		
		for( int i = 0; i < size; i++ ) {
			int[] insVec = getNewInsertionVector();
			
			List<T> ranking = getInsertedList(insVec);
											
			ranking.toArray(profile[i]);
		}
		
		return new PreferenceProfile<T>(profile);
	}

}
