/**
 * 
 */
package net.andrewmao.misc;

import java.io.Serializable;

public class Pair<T1, T2> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3727118342998231564L;
	
	public final T1 t1;
	public final T2 t2;
	
	public Pair(T1 t1, T2 t2) {
		this.t1 = t1;
		this.t2 = t2;
	}
	
	public boolean equals(Pair<T1, T2> other) {
		return this.t1 == other.t1 && this.t2 == other.t2;
	}
}