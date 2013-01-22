package net.andrewmao.models.noise;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.andrewmao.models.discretechoice.ScoredItems;

public abstract class RandomUtilityModel<T> extends NoiseModel<T> {
		
	protected final ScoredItems<T> strMap;
	protected final double[] strParams;

	public RandomUtilityModel(ScoredItems<T> strengths, Random rnd) {
		super(new ArrayList<T>(strengths.keySet()), rnd);
		this.strMap = strengths;
		
		strParams = new double[candidates.size()];
		
		for( int j = 0; j < strParams.length; j++ )
			strParams[j] = strMap.get(candidates.get(j)).doubleValue();
	}
	
	public RandomUtilityModel(List<T> candidates, Random rnd, double[] strParams) {
		super(candidates, rnd);
		
		if( candidates.size() != strParams.length )
			throw new RuntimeException("Must have same number of strength parameters as candidates");
		
		this.strMap = new ScoredItems<T>(candidates, strParams);
		this.strParams = strParams;		
	}
	
	public RandomUtilityModel(List<T> candidates, Random rnd, double adjStrDiff) {
		super(candidates, rnd);
		
		this.strMap = new ScoredItems<T>(candidates);
		this.strParams = new double[candidates.size()];
		
		for( int j = 0; j < strParams.length; j++ ) {
			double str = -j * adjStrDiff;
			strParams[j] = str;
			strMap.get(candidates.get(j)).setValue(str);
		}
	}

}
