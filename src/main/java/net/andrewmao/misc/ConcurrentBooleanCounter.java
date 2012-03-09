package net.andrewmao.misc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentBooleanCounter<T> extends ConcurrentHashMap<T, Boolean> {
	private static final long serialVersionUID = -7951600307606236355L;

	private AtomicInteger trueCount;
	
	public ConcurrentBooleanCounter() {
		trueCount = new AtomicInteger(0);
	}

	public int getTrueCount() { return trueCount.get(); }
	
	public int getFalseCount() {
		// Slightly trickier
		synchronized(this) {
			return this.size() - getTrueCount();
		}		
	}
	
	@Override
	public void clear() {
		synchronized(this) {
			super.clear();
			trueCount.set(0);
		}	
	}

	@Override
	public Boolean put(T key, Boolean value) {
		// TODO this may no longer be correct after removing synchronized block around the whole thing
		
		if( value == true ) {
			// Add 1 if it doesn't exist or it was false before
			if( !super.containsKey(key) || super.get(key) == false) trueCount.incrementAndGet();
		}
		else {
			// Subtract 1 if it did exist and was true before
			if( super.containsKey(key) && super.get(key) == true ) trueCount.decrementAndGet();
		}

		return super.put(key, value);
	}

	@Override
	public Boolean remove(Object key) {
		Boolean value = super.remove(key);		
		if( value != null && value == true ) trueCount.decrementAndGet();		
		return value;
	}	
	
}
