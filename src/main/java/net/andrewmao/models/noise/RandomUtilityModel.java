package net.andrewmao.models.noise;

import java.util.ArrayList;
import java.util.List;

import net.andrewmao.models.discretechoice.ScoredItems;
import net.andrewmao.socialchoice.rules.SocialChoiceMetric;

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
	
	public double computeMetric(SocialChoiceMetric<T> metric) {
		return metric.computeByScore(strMap);
	}
}
