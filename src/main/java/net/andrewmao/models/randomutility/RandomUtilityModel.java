package net.andrewmao.models.randomutility;

import java.util.Collections;
import java.util.List;

public abstract class RandomUtilityModel<T> {
	
	protected final List<T> items;
	
	protected RandomUtilityModel(List<T> items) {
		this.items = Collections.unmodifiableList(items);
	}
	
	public List<T> getItems() { return items; }
	
	public abstract double[] getParameters();	
	
}
