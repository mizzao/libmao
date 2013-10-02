package net.andrewmao.models.noise;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.andrewmao.probability.NormalDist;

import org.apache.commons.math3.analysis.function.Sqrt;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class NormalNoiseTest {

	static final double EXACT_TOL = 1e-12;
	
	static final int trials = 10;
	static final List<Character> stuff = Arrays.asList(new Character[] { 'a', 'b', 'c', 'd' });
	
	RealVector mean, var;
	NormalNoiseModel<Character> model;
	
	@Parameters
	public static Collection<Object[]> tnImpls() {											
		return TestParameterGen.randomMeanVarRankings(stuff.size(), trials);
	}
	
	public NormalNoiseTest(RealVector mean, RealVector var, int[] ranking) {
		this.mean = mean;
		this.var = var;
		
		this.model = new NormalNoiseModel<>(stuff, mean.toArray(), var.map(new Sqrt()).toArray());
		
		System.out.println();
	}
	
	@Test
	public void testModelProbs() {
		for( int i = 0; i < stuff.size(); i++ ) {
			for( int j = i+1; j < stuff.size(); j++ ) {
				// lose prob is that i loses to j: x_i < x_j or x_i - x_j < 0
				double loseProb = NormalDist.cdf(mean.getEntry(i) - mean.getEntry(j), 
						Math.sqrt(var.getEntry(i) + var.getEntry(j)), 0);
				double winProb = 1 - loseProb;
				
				assertEquals(winProb, model.marginalProbability(stuff.get(i), stuff.get(j)), EXACT_TOL);
				assertEquals(loseProb, model.marginalProbability(stuff.get(j), stuff.get(i)), EXACT_TOL);
			}
		}
	}
}
