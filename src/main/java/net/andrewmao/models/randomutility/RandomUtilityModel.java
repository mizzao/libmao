package net.andrewmao.models.randomutility;

public interface RandomUtilityModel<T> {

	void addData(T winner, T loser, int count);
	
	double[] getParameters();	
}
