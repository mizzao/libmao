package net.andrewmao.models.noise;

import net.andrewmao.socialchoice.rules.PreferenceProfile;

public interface Estimator<M extends NoiseModel<?>> {

	<T> M fitModel(PreferenceProfile<T> profile);
	
}
