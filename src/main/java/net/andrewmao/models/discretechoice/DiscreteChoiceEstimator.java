package net.andrewmao.models.discretechoice;

import net.andrewmao.models.noise.Estimator;
import net.andrewmao.models.noise.NoiseModel;

/**
 * Base class for a group of models that assign hidden underlying strengths 
 * to a set of objects 
 * 
 * @author mao
 *
 * @param <T>
 */
public abstract class DiscreteChoiceEstimator<M extends NoiseModel<?>> implements Estimator<M> {		
		
}
