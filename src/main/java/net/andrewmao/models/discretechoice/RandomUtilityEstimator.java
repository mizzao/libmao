package net.andrewmao.models.discretechoice;

import java.util.List;

import net.andrewmao.models.noise.NoiseModel;
import net.andrewmao.models.noise.OrdinalEstimator;

public abstract class RandomUtilityEstimator<M extends NoiseModel<?>, P> 
implements OrdinalEstimator<M> {
	
	public abstract P getParameters(List<int[]> rankings, int numItems);
		
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
