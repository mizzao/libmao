package net.andrewmao.probability;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.exception.OutOfRangeException;

/**
 * Truncated normal distribution, mostly from
 * http://en.wikipedia.org/wiki/Truncated_normal_distribution
 * However, that page has some errors, which have been corrected.
 * 
 * @author mao
 *
 */
public class TruncatedNormal extends AbstractRealDistribution {

	private static final long serialVersionUID = 3279850685379757823L;

	private final NormalDistribution normal;
	
	private final double mu;
	private final double sigma;	
	private final double cdf_a;
	private final double cdf_b;
	private final double Z;
	
	private final double alpha;
	private final double beta;
	private final double aa;
	private final double bb;
	
	public TruncatedNormal(double mean, double sd, double lb, double ub) {
		if( mean == Double.NaN || sd == Double.NaN ||
				lb == Double.NaN || ub == Double.NaN ) 
			throw new IllegalArgumentException("Cannot take NaN as an argument");
		
		normal = new NormalDistribution(mean, sd);
		
		mu = mean;
		sigma = sd;
		cdf_a = normal.cumulativeProbability(lb);
		cdf_b = normal.cumulativeProbability(ub);
		Z = cdf_b - cdf_a;
		
		alpha = (lb - mu) / sigma;
		beta = (ub - mu) / sigma;
		this.aa = lb;
		this.bb = ub;
	}
	
	@Override
	public double probability(double x) {		
		return 0;
	}

	@Override
	public double density(double x) {
		if( x <= aa || x >= bb ) return 0;
		
		return normal.density(x) / (sigma * Z);
	}

	@Override
	public double cumulativeProbability(double x) {
		if( x <= aa ) return 0;
		else if( x >= bb ) return 1;
			
		return (normal.cumulativeProbability(x) - cdf_a) / Z;
	}

	@Override
	public double inverseCumulativeProbability(double p) throws OutOfRangeException {		
		if (p < 0.0 || p > 1.0) throw new OutOfRangeException(p, 0, 1);        
		
		return normal.inverseCumulativeProbability(p * Z + cdf_a);
	}

	@Override
	public double getNumericalMean() {		
		return mu + (normal.density(aa) - normal.density(bb)) * sigma / Z;
	}

	@Override
	public double getNumericalVariance() {
		double sq = ( normal.density(aa) - normal.density(bb) ) / Z;
		double br = 1 + ( alpha * normal.density(aa) - beta * normal.density(bb) ) / Z - sq * sq;  
		return sigma * sigma * br;
	}

	@Override
	public double sample() {			
		double val = randomData.nextUniform(0, 1) * Z + cdf_a;
		
		return normal.inverseCumulativeProbability(val);
	}

	@Override
	public double getSupportLowerBound() {		
		return aa;
	}

	@Override
	public double getSupportUpperBound() {		
		return bb;
	}

	@Override
	public boolean isSupportLowerBoundInclusive() {		
		return false;
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {		
		return false;
	}

	@Override
	public boolean isSupportConnected() {		
		return true;
	}

}
