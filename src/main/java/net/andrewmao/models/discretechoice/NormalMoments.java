package net.andrewmao.models.discretechoice;

/**
 * A convenience class for returning multiple things
 * @author mao
 *
 */
class NormalMoments {
	
	final Double cdf;
	final double[] m1;
	final double[] m2;
	
	int weight;
	
	private NormalMoments(double[] m1, double[] m2, Double cdf, int weight) {
		this.m1 = m1;
		this.m2 = m2;
		this.cdf = cdf;
		this.weight = weight;
	}
	
	NormalMoments(double[] m1, double[] m2, double cdf) {
		this(m1, m2, cdf, 1);
	}
	
	NormalMoments(double[] m1, double[] m2, int weight) {
		this(m1, m2, null, weight);
	}
		
	NormalMoments(double[] m1, double cdf, int weight) {
		this(m1, null, cdf, weight);
	}
	
	NormalMoments(double[] m1, double cdf) {
		this(m1, null, cdf, 1);
	}
	
	NormalMoments(double[] m1, int weight) {
		this(m1, null, null, weight);
	}
	
	void setWeight(int weight) {
		this.weight = weight;
	}
	
}