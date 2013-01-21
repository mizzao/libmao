package net.andrewmao.models.discretechoice;

import java.util.Collections;
import java.util.List;

/**
 * Base class for a group of models that assign hidden underlying strengths 
 * to a set of objects 
 * 
 * @author mao
 *
 * @param <T>
 */
public abstract class DiscreteChoiceModel<T> {
	
	protected final List<T> items;
	
	protected DiscreteChoiceModel(List<T> items) {
		this.items = Collections.unmodifiableList(items);
	}
	
	public List<T> getItems() { return items; }
	
	public abstract ScoredItems<T> getParameters();	
}
