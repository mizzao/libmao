package net.andrewmao.math;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class RandomSelection {

	private static Random rnd = new Random();

	/**
	 * Returns a subset of size k chosen at random from n elements from 0 to n-1, inclusive. 
	 * @param n
	 * @param k
	 * @return
	 */
	public static int[] randNK(int n, int k, Random rnd) {
		if( rnd == null ) rnd = RandomSelection.rnd;
		
		// Fix bad inputs		
		if( k > n ) k = n;
		else if( k < 0 ) k = 0;
		
		int i, sw, temp;
		int[] arr = new int[n];
	
		for(i = 0; i < n; i++) arr[i] = i;
		
		// Randomly arrange the first k elements
		for(i = 0; i < k; i++) {
			sw = i + rnd.nextInt(n - i);
			
			// Swap
			temp = arr[sw];
			arr[sw] = arr[i];
			arr[i] = temp;
		}
		
		return Arrays.copyOf(arr, k);
	}
	
	/**
	 * The random stream selection algorithm	
	 * @param <T>
	 * @param it
	 * @return
	 */	
	public static <T> T selectRandom(Iterable<T> it) {		
		int count = 0;		
		T selected = null;	
				
		for( T current : it ) {
			count += 1;
			
			if( rnd.nextDouble() < 1.0/count ) {
				selected = current;
			}			
		}
		return selected;		
	}

	/**
	 * Selects indices from an array based on weights.
	 * 
	 * @param wts
	 * @param rnd 
	 * @return
	 */
	public static int selectRandomWeighted(double[] wts, Random rnd) {
		int selected = 0;
		double total = wts[0];
		
		for( int i = 1; i < wts.length; i++ ) {
			total += wts[i];
			
			if( rnd.nextDouble() <= (wts[i] / total)) {
				selected = i;
			}
		}
		
		return selected;		
	}
	
	/**
	 * N choose K from a list.
	 * @param <T>
	 * @param list
	 * @param k
	 * @return
	 */
	public static <T> Collection<T> selectKRandom(List<T> list, int k) {				
		int[] indices = randNK(list.size(), k, rnd);
		
		Collection<T> stuff = new LinkedList<T>();
		for( int i = 0; i < indices.length; i++ ) {
			stuff.add(list.get(indices[i]));
		}
		
		return stuff;
	}
	
	/**
	 * N choose K from a list.
	 * @param <T>
	 * @param list
	 * @param k
	 * @param rnd
	 * @return
	 */
	public static <T> Collection<T> selectKRandom(List<T> list, int k, Random rnd) {
		if( rnd == null ) rnd = RandomSelection.rnd;
		
		int[] indices = randNK(list.size(), k, rnd);
		
		Collection<T> stuff = new LinkedList<T>();
		for( int i = 0; i < indices.length; i++ ) {
			stuff.add(list.get(indices[i]));
		}
		
		return stuff;
	}
	
	/**
	 * N choose K from an array.
	 * @param <T>
	 * @param arr
	 * @param k
	 * @return
	 */	
	public static <T> T[] selectKRandom(T[] arr, int k, Random rnd) {
		int[] indices = randNK(arr.length, k, rnd);						
		
		T[] stuff = Arrays.copyOf(arr, k);
		
		for( int i = 0; i < indices.length; i++ ) {
			stuff[i] = arr[indices[i]];
		}
		
		return stuff;
	}
	
	/**
	 * Randomly shuffles an array of integers in place
	 * @param arr
	 * @param rnd
	 * @return
	 */
	public static int[] shuffle(int[] arr, Random rnd) {
		if( rnd == null ) rnd = RandomSelection.rnd;
		
		int temp;
		
		for( int i = arr.length - 1; i > 0; i-- ) {
			int j = rnd.nextInt(i + 1);
			
			temp = arr[j];
			arr[j] = arr[i];
			arr[i] = temp;
		}
		
		return arr;
	}

	public static long[] shuffle(long[] arr, Random rnd) {
		if( rnd == null ) rnd = RandomSelection.rnd;
		
		long temp;
		
		for( int i = arr.length - 1; i > 0; i-- ) {
			int j = rnd.nextInt(i + 1);
			
			temp = arr[j];
			arr[j] = arr[i];
			arr[i] = temp;
		}
		
		return arr;
	}

	/**
	 * Randomly shuffles an array in place
	 * @param <T>
	 * @param arr
	 * @param rnd
	 * @return
	 */
	public static <T> T[] shuffle(T[] arr, Random rnd) {
		if( rnd == null ) rnd = RandomSelection.rnd;
		
		T temp;
		
		for( int i = arr.length - 1; i > 0; i-- ) {
			int j = rnd.nextInt(i + 1);
			
			temp = arr[j];
			arr[j] = arr[i];
			arr[i] = temp;
		}
		
		return arr;
	}

	/**
	 * Generates a random permutation of numbers from 0 to size - 1
	 * @param size
	 * @return
	 */
	public static int[] randomShuffle(int size) {
		int[] arr = new int[size];
		
		for( int i = 0; i < size; i++ ) arr[i] = i;
		RandomSelection.shuffle(arr, null);
		
		return arr;
	}
	
	/**
	 * Generates a random permutation of numbers from 0 to size - 1
	 * @param size
	 * @param rnd
	 * @return
	 */
	public static int[] randomShuffle(int size, Random rnd) {
		int[] arr = new int[size];
		
		for( int i = 0; i < size; i++ ) arr[i] = i;
		RandomSelection.shuffle(arr, rnd);
		
		return arr;
	}

	/**
	 * Generates a random shuffle of a list
	 * @param <T>
	 * @param stuff
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> randomShuffle(List<T> stuff) {
		T[] arr = (T[]) stuff.toArray();		
		RandomSelection.shuffle(arr, null);		
		return Arrays.asList(arr);
	}

	/**
	 * Generates an array of n items, each independently from 0 to c-1, 
	 * which are always the same with the same seed
	 * 
	 * @param n
	 * @param c
	 * @param seed
	 * @return
	 */
	public static int[] randomSeededMultiset(int n, int c, long seed) {
		int[] arr = new int[n];
		Random seededRand = new Random(seed);
		for( int i = 0; i < n; i++ ) arr[i] = seededRand.nextInt(c);
		return arr;
	}

}
