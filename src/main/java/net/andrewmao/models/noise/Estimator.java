package net.andrewmao.models.noise;

import net.andrewmao.socialchoice.rules.PreferenceProfile;

public interface Estimator<N extends NoiseModel<?>> {

	<T> N fitModel(PreferenceProfile<T> profile);
	
}
