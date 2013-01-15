package net.andrewmao.models.discretechoice;

import java.util.List;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * PLMM algorithm from 
 * http://sites.stat.psu.edu/~dhunter/code/btmatlab/plackmm.m
 * 
 * @author mao
 *
 * @param <T>
 */
public class PlackettLuceModel<T> extends RandomUtilityModel<T> {

	static final double tolerance = 1e-9;
	
	public PlackettLuceModel(List<T> items) {
		super(items);		
	}

	@Override
	public ScoredItems<T> getParameters() {
		RealVector diff = null, gamma = new ArrayRealVector(items.size(), 1);		
		
		do {
			
		} while( diff.getNorm() > tolerance );
		
		return new ScoredItems<T>(items, gamma.toArray());
	}

}
