package net.andrewmao.models.noise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import net.andrewmao.models.discretechoice.ScoredItems;
import net.andrewmao.socialchoice.rules.PreferenceProfile;
import net.andrewmao.socialchoice.rules.RankingMetric;

public abstract class RandomUtilityModel<T> extends NoiseModel<T> {
		
	protected final ScoredItems<T> strMap;
	protected final double[] strParams;

	public RandomUtilityModel(ScoredItems<T> strengths) {
		super(new ArrayList<T>(strengths.keySet()));
		this.strMap = strengths;
		
		strParams = new double[candidates.size()];
		
		for( int j = 0; j < strParams.length; j++ )
			strParams[j] = strMap.get(candidates.get(j)).doubleValue();
	}
	
	public RandomUtilityModel(List<T> candidates, double[] strParams) {
		super(candidates);
		
		if( candidates.size() != strParams.length )
			throw new RuntimeException("Must have same number of strength parameters as candidates");
		
		this.strMap = new ScoredItems<T>(candidates, strParams);
		this.strParams = strParams;		
	}
	
	public RandomUtilityModel(List<T> candidates, double adjStrDiff) {
		super(candidates);
		
		this.strMap = new ScoredItems<T>(candidates);
		this.strParams = new double[candidates.size()];
		
		for( int j = 0; j < strParams.length; j++ ) {
			double str = -j * adjStrDiff;
			strParams[j] = str;
			strMap.get(candidates.get(j)).setValue(str);
		}
	}

	public double[] getValues() {
		return strParams;
	}
	
	public ScoredItems<T> getValueMap() {
		return strMap;
	}
	
	/**
	 * Sample random utilities in the same order as the initialized candidates
	 * @param rnd
	 * @return
	 */
	public abstract double[] sampleUtilities(Random rnd);
	
	@Override
	public PreferenceProfile<T> sampleProfile(int size, Random rnd) {
		T[][] profile = super.getProfileArrayInitialized(size);							
		
		for( int i = 0; i < size; i++ ) {																	
			double[] strVals = sampleUtilities(rnd);

			// Sort by the resulting strength parameters
			sortByStrengths(profile[i], strVals);			
		}
		
		return new PreferenceProfile<T>(profile);
	}
	
	// Higher strength parameter comes earlier in the array
	void sortByStrengths(T[] arr, final double[] strengths) {
		Arrays.sort(arr, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				int i1 = candidates.indexOf(o1);
				int i2 = candidates.indexOf(o2);
				
				return Double.compare(strengths[i2], strengths[i1]);
			}				
		});
	}
	
	/* Reverse sort order - lower exponential comes first
	 * so it's the same as normal sort order
	 */
	void sortByStrengthReverse(T[] arr, final double[] strengths) {
		Arrays.sort(arr, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				int i1 = candidates.indexOf(o1);
				int i2 = candidates.indexOf(o2);
				
				return Double.compare(strengths[i1], strengths[i2]);
			}				
		});
	}

	public double computeMLMetric(RankingMetric<T> metric) {
		return metric.computeByScore(strMap);
	}

}
