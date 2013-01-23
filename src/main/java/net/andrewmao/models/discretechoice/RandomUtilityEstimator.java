package net.andrewmao.models.discretechoice;

import java.util.ArrayList;
import java.util.List;

import net.andrewmao.models.noise.NoiseModel;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

public abstract class RandomUtilityEstimator<M extends NoiseModel<?>> extends DiscreteChoiceEstimator<M> {	
	
	protected <T> List<int[]> getIndices(PreferenceProfile<T> profile, List<T> ordering) {		
		List<int[]> rankings = new ArrayList<int[]>();
		
		for( T[] preference : profile.getProfile()) {
			int[] ranking = new int[preference.length];		
			int i = 0;
			for( T item : preference ) ranking[i++] = ordering.indexOf(item) + 1;		
			rankings.add(ranking);		
		}
		
		return rankings;
	}
	
	public abstract double[] getParameters(List<int[]> rankings, int numItems);
	
}
