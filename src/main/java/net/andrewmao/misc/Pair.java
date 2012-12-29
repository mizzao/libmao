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
	
	@Override
	public boolean equals(Object other) {
		if( other instanceof Pair ) {
			return equals((Pair<?, ?>) other);
		}
		else {
			return false;
		}
	}
	
	public String toString() {
		return "(" + t1.toString() + ", " + t2.toString() + ")";
	}
	
	public <O1, O2> boolean equals(Pair<O1, O2> other) {
		return this.t1.equals(other.t1) && this.t2.equals(other.t2);
	}
	
	@Override
	public int hashCode() {
		return t1.hashCode() + t2.hashCode();
	}
}
