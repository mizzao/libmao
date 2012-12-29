/**
 * An exponential distribution
 */
package net.andrewmao.stat;

import java.util.Random;

/**
 * @author mao
 *
 */
public class Exponential {
	
	private Random r;
	private volatile double lambda;	
	
	public Exponential(double l) {
		lambda = l;
		r = new Random();
	}
	
	public void setLambda(double l) {
		lambda = l;
	}
	
	public double sample() {
		return (-Math.log(r.nextDouble())) / lambda;		
	}

}
