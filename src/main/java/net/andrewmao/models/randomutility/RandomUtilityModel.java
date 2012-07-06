package net.andrewmao.models.randomutility;

import org.apache.commons.math.MathException;

public interface RandomUtilityModel<T> {

	void addData(T winner, T loser, int count);
	
	double[] getParameters() throws MathException;	
}
