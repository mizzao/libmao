package net.andrewmao.math;

public class NumericUtils {

	/**
	 * Computes log(sum{i in input} exp(a_i)).
	 * 
	 * See http://jblevins.org/notes/log-sum-exp
	 *  
	 * We shift by max instead of average since log probabilities are always negative. 
	 * 
	 * expm1 is used since log probs are shifted closer to 0.
	 * 
	 * @param logProbs
	 * @return
	 */
	public static double getLogSumExp(double[] logProbs) {		
		double max = Double.NEGATIVE_INFINITY;
		for( double d : logProbs ) max = Math.max(d, max);		
				
		double sum = 0;		
		for( double d : logProbs ) sum += Math.expm1(d - max) + 1;
		
		return max + Math.log(sum);
	}
	
}
