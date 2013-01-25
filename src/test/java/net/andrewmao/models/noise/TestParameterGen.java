package net.andrewmao.models.noise;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.andrewmao.math.RandomSelection;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class TestParameterGen {

	static Random rnd = new Random();
	
	public static RealVector randomMeanVector(int n) {
		RealVector mean = new ArrayRealVector(n);
		for( int j = 0; j < n; j++ ) {
			mean.setEntry(j, rnd.nextDouble() * 2 - 1);			
		}
		return mean;
	}
	
	public static RealVector randomVarVector(int n) {
		RealVector var = new ArrayRealVector(n);
		for( int j = 0; j < n; j++ ) {			
			var.setEntry(j, rnd.nextDouble() * 2);
		}
		return var;
	}

	public static List<Object[]> randomMeanVarRankings(int m, int trials) {
		List<Object[]> stuff = new ArrayList<Object[]>(trials);
						
		for( int i = 0; i < trials; i++ ) {
			int[] arr = new int[m];
			for( int j = 0; j < m; j++ ) arr[j] = j+1;
			
			RandomSelection.shuffle(arr, rnd);		
			
			stuff.add(new Object[] {randomMeanVector(m), randomVarVector(m), arr } );
		}
		
		return stuff;
	}

	public static List<Object[]> randomMeanVarProfiles(int m, int trials, int n) {
		List<Object[]> stuff = new ArrayList<Object[]>(trials);
		
		for( int i = 0; i < trials; i++ ) {												
			int[] arr = new int[m];
			for( int j = 0; j < m; j++ ) 
				arr[j] = j+1;
			
			List<int[]> rankings = new ArrayList<int[]>(n);
			for( int j = 0; j < n; j++ ) 
				rankings.add(RandomSelection.shuffle(arr.clone(), rnd));
			
			stuff.add(new Object[] {randomMeanVector(m), randomVarVector(m), rankings } );
		}
		
		return stuff;
	}

	@SuppressWarnings("unchecked")
	public static <T> Collection<Object[]> randomPairwiseProfiles(T[] items, int trials, int maxWins) {
		
		T[] items_rev = items.clone();
		T temp = items_rev[0];
		items_rev[0] = items_rev[1];
		items_rev[1] = temp;
		
		List<Object[]> stuff = new ArrayList<Object[]>(trials);
		
		for( int i = 0; i < trials; i++ ) {
			// Add 1 win to each side to ensure property for PLMM.
			int wins_0 = 1 + rnd.nextInt(maxWins);
			int wins_1 = 1 + rnd.nextInt(maxWins);
							
			int n = wins_0 + wins_1;		
			T[][] profile = (T[][]) Array.newInstance(items.getClass(), n);
			
			int j = 0;
			for( ; j < wins_0; j++ ) {
				profile[j] = items;
			}				
			
			for( ; j < n; j++ ) {
				profile[j] = items_rev;				
			}							
			
			stuff.add(new Object[] {new PreferenceProfile<T>(profile)});	
		}
			
		return stuff;
	}		
	
}
