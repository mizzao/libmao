package net.andrewmao.models.noise;

import net.andrewmao.socialchoice.rules.PreferenceProfile;

public interface OrdinalEstimator<M extends NoiseModel<?>> {

	<T> M fitModelOrdinal(PreferenceProfile<T> profile);
	
}
