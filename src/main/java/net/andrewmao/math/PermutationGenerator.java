package net.andrewmao.math;

public interface PermutationGenerator {

	public void reset();

	boolean hasMore();

	int[] getNext();
	
}
