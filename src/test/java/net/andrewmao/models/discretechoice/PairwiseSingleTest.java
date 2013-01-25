package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.andrewmao.probability.NormalDist;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PairwiseSingleTest {
	
	static int trials = 50;
	static double large_num = 1e6;
	
	static ThurstoneMostellerModel tm = new ThurstoneMostellerModel();
	static BradleyTerryModel bt = new BradleyTerryModel();
	
	double prob;
	
	public PairwiseSingleTest(double prob) {
		this.prob = prob;
	}
	
	@Parameters
	public static Collection<Object[]> probs() {
		List<Object[]> list = new ArrayList<Object[]>(trials);
		for( int i = 0; i < trials; i++ ) {
			list.add(new Object[] { Math.random() } );
		}
		return list;
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testThurstoneMosteller() {
		double betterWins = prob * large_num;
		double worseWins = (1-prob) * large_num;
		
		double strDiff = NormalDist.inverseF01(prob);
		
		double[][] winMatrix = new double[2][2];
		winMatrix[0][1] = betterWins;
		winMatrix[1][0] = worseWins;
		
		double[] params = tm.getParameters(winMatrix);
		
		assertEquals(strDiff, params[0] - params[1], 0.01);
	}
	
	@Test
	public void testBradleyTerry() {
		double betterWins = prob * large_num;
		double worseWins = (1-prob) * large_num;
		
		double strDiff = Math.log(prob) - Math.log(1-prob);
		
		double[][] winMatrix = new double[2][2];
		winMatrix[0][1] = betterWins;
		winMatrix[1][0] = worseWins;
		
		double[] params = bt.getParameters(winMatrix);
		double measuredDiff = params[0] - params[1];
		
		if( measuredDiff == 0.0 ) {
			System.out.println("Strange result: ");
			System.out.println(prob);
			System.out.println(Arrays.deepToString(winMatrix));
		}			
		
		assertEquals(strDiff, measuredDiff, 0.01);
	}

}
