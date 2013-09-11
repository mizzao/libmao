package net.andrewmao.models.games;

import java.text.NumberFormat;

import be.ac.ulg.montefiore.run.jahmm.Observation;

public class SignalActionObservation<S extends Enum<S>, A extends Enum<A>> 
extends Observation {

	public final S signal;
	public final A action;
	
	public SignalActionObservation(S signal, A action) {
		this.signal = signal;
		this.action = action;
	}
	
	public String toString() {
		return "(S: " + signal + 
				", A: " + action + ")";
	}
	
	@Override
	public String toString(NumberFormat numberFormat) {		
		return toString();
	}

}
