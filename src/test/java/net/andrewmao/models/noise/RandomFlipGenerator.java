package net.andrewmao.models.noise;

import java.util.List;
import java.util.Random;

import net.andrewmao.models.noise.NoiseModel;
import net.andrewmao.socialchoice.rules.PreferenceProfile;


/**
 * This is a crap preference generator used for testing that doesn't 
 * implement any useful mathematical properties
 * 
 * @author mao
 *
 * @param <T>
 */
public class RandomFlipGenerator<T> extends NoiseModel<T> {
	
	private final double prob;
	
	public RandomFlipGenerator(List<T> candidates, double prob) {
		super(candidates, new Random());
				
		this.prob = prob;
	}
		
	@Override
	public PreferenceProfile<T> sampleProfile(int size) {
		T[][] profile = super.getProfileArray(size);
		
		for( int i = 0; i < size; i++ ) {
			T[] stuff = candidates.toArray(profile[i]);
			
			for( int a = 0; a < stuff.length; a++ ) {
				for( int b = 0; b < stuff.length; b++ ) {
					if( a != b && rnd.nextDouble() < prob ) {
						T temp = stuff[a];
						stuff[a] = stuff[b];
						stuff[b] = temp;
					}
				}
			}						
		}
		
		return new PreferenceProfile<T>(profile);
	}

	@Override
	public double logLikelihood(PreferenceProfile<T> profile) {
		throw new UnsupportedOperationException();
	}

}