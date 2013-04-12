package net.andrewmao.models.discretechoice;

import java.util.List;

import net.andrewmao.models.noise.NoiseModel;
import net.andrewmao.models.noise.OrdinalEstimator;

public abstract class RandomUtilityEstimator<M extends NoiseModel<?>> 
implements OrdinalEstimator<M> {
	
	public abstract double[] getParameters(List<int[]> rankings, int numItems);
	
}
