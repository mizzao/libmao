package net.andrewmao.models.discretechoice;

import java.util.ArrayList;
import java.util.List;

public abstract class RandomUtilityModel<T> extends DiscreteChoiceModel<T> {

	List<int[]> rankings;
	
	protected RandomUtilityModel(List<T> items) {
		super(items);		
		
		rankings = new ArrayList<int[]>();	
	}
		
	public final void addData(List<T> list) {
		int[] ranking = new int[list.size()];		
		int i = 0;
		for( T item : list ) ranking[i++] = items.indexOf(item) + 1;		
		rankings.add(ranking);
	}
		
	public final void addData(T[] arr) {
		int[] ranking = new int[arr.length];		
		int i = 0;
		for( T item : arr ) ranking[i++] = items.indexOf(item) + 1;		
		rankings.add(ranking);		
	}
	
}
