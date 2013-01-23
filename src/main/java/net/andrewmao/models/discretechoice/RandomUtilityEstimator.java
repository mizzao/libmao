package net.andrewmao.models.discretechoice;

import java.util.List;

import net.andrewmao.models.noise.NoiseModel;

public abstract class RandomUtilityEstimator<M extends NoiseModel<?>> extends DiscreteChoiceEstimator<M> {	
	
	public abstract double[] getParameters(List<int[]> rankings, int numItems);
	
}
