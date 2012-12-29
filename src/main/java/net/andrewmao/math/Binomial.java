package net.andrewmao.math;

/**
 * Binomial distribution
 * 
 * @author mao
 *
 */
public class Binomial {
	
	private int n;
	private double p;
	
	public Binomial(int n, double p) {
		this.n = n;
		this.p = p;
	}
	
	public void setN(int n) { this.n = n; }
	
	public void setP(int p) { this.p = p; }
	
	public int sample() {
		int k = 0;
		for( int i = 0; i < n; i++ ) {
			if( Math.random() <= p) k++;
		}
		return k;	
	}
	
}
