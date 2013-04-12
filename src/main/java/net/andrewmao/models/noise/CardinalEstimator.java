package net.andrewmao.models.noise;

import java.util.List;

public interface CardinalEstimator<M extends NoiseModel<?>> {

	<T> M fitModelCardinal(List<T> items, double[][] scores);
	
}
