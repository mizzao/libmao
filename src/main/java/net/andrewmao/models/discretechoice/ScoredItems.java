package net.andrewmao.models.discretechoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.mutable.MutableDouble;

public class ScoredItems<T> extends LinkedHashMap<T, MutableDouble> {

	private static final long serialVersionUID = -6171230363427632120L;

	public ScoredItems(List<T> items, double[] scores) {
		super();
		
		if( items.size() != scores.length ) 
			throw new IllegalArgumentException("Items must have a corresponding array of values");
		
		for( int i = 0; i < scores.length; i++ ) 
			put(items.get(i), new MutableDouble(scores[i]));
	}

	public ScoredItems(T[] items, double[] scores) {
		super();
		
		if( items.length != scores.length )
			throw new IllegalArgumentException("Items must have a corresponding array of values");
		
		for( int i = 0; i < scores.length; i++ )
			put(items[i], new MutableDouble(scores[i]));
	}
	
	public ScoredItems(T[] items, int[] scores) {
		super();
		
		if( items.length != scores.length )
			throw new IllegalArgumentException("Items must have a corresponding array of values");
		
		for( int i = 0; i < scores.length; i++ )
			put(items[i], new MutableDouble(scores[i]));
	}

	/**
	 * Creates items with a default score of 0
	 * @param items
	 */
	public ScoredItems(Collection<T> items) {
		super();
		
		for( T item : items )
			put(item, new MutableDouble());
	}

	/**
	 * Creates items with a default score of 0
	 * @param items
	 */
	public ScoredItems(T[] items) {
		super();
		
		for( int i = 0; i < items.length; i++ )
			put(items[i], new MutableDouble());
	}

	public List<T> getRanking() {		
		List<T> ranking = new ArrayList<T>(keySet());
		
		Collections.sort(ranking, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {				
				// Reverse comparison order - higher scores come first
				return get(o2).compareTo(get(o1));
			}			
		});
		
		return ranking;
	}

	/**
	 * 
	 * @return an array in the same order as the original items.
	 */
	public double[] toArray() {
		double[] scores = new double[size()];
		
		int i = 0;		
		for( MutableDouble score : values() )
			scores[i++] = score.doubleValue();
			
		return scores;
	}
	
}
