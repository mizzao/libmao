package net.andrewmao.math;

public class SmallPermutationGenerator implements PermutationGenerator {

	private int[] a;
	private long numLeft;
	private long total;

	//-----------------------------------------------------------
	// Constructor. WARNING: Don't make n too large.
	// Recall that the number of permutations is n!
	// which can be very large, even when n is as small as 20 --
	// 20! = 2,432,902,008,176,640,000 and
	// 21! is too big to fit into a Java long, which is
	// why we use BigInteger instead.
	//----------------------------------------------------------

	public SmallPermutationGenerator (int n) {
		if (n < 1) {
			throw new IllegalArgumentException ("Min 1");
		}
		else if( n > 20 ) {
			throw new IllegalArgumentException ("Too big");
		}
		a = new int[n];
		total = getFactorial (n);
		reset ();
	}
	
	@Override
	public void reset() {
		for (int i = 0; i < a.length; i++) {
			a[i] = i;
		}
		numLeft = total;
	}
	
	public long getNumLeft () {
		return numLeft;
	}

	public long getTotal () {
		return total;
	}
	
	@Override
	public boolean hasMore() {		
		return numLeft > 0;
	}

	private static long getFactorial (int n) {
		long fact = 1L;
		for (int i = n; i > 1; i--) {
			fact = fact * i;
		}
		return fact;
	}
	
	@Override
	public int[] getNext() {
		if ( numLeft == total ) {
			numLeft--;
			return a;
		}

		int temp;

		// Find largest index j with a[j] < a[j+1]
		int j = a.length - 2;
		while (a[j] > a[j+1]) {
			j--;
		}

		// Find index k such that a[k] is smallest integer
		// greater than a[j] to the right of a[j]
		int k = a.length - 1;
		while (a[j] > a[k]) {
			k--;
		}

		// Interchange a[j] and a[k]
		temp = a[k];
		a[k] = a[j];
		a[j] = temp;

		// Put tail end of permutation after jth position in increasing order
		int r = a.length - 1;
		int s = j + 1;

		while (r > s) {
			temp = a[s];
			a[s] = a[r];
			a[r] = temp;
			r--;
			s++;
		}

		numLeft--;
		return a;
	}

}
