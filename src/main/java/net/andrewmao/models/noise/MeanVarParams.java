package net.andrewmao.models.noise;

public class MeanVarParams {

	public final double[] mean;
	public final double[] variance;
	public final double fittedLikelihood;
	
	public MeanVarParams(double[] mean, double[] variance, double likelihood) {
		this.mean = mean;
		this.variance = variance;
		this.fittedLikelihood = likelihood;
	}
	
}
