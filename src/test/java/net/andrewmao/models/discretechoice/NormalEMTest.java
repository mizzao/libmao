package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.andrewmao.models.noise.NormalNoiseModel;
import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.junit.*;

public class NormalEMTest {
	
	static Random rnd = new Random();
	
	Character[] stuff = new Character[] { 'A', 'B', 'C', 'D' };
	final List<Character> stuffList = Arrays.asList(stuff);
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testInference() {		
		// TODO: this is still busted
		
		int size = 10000;
		double tol = 0.02;		
		 					
		double strDiff = 1.0;
		System.out.println("Testing " + strDiff);

		NormalNoiseModel<Character> gen = new NormalNoiseModel<Character>(stuffList, new Random(), strDiff, 1.0d);			

		PreferenceProfile<Character> prefs = gen.sampleProfile(size);	
		double targetLL = gen.logLikelihood(prefs);
		System.out.println("Target likelihood: " + targetLL);

		OrderedNormalEM normalEM = new OrderedNormalEM(100, 1e-5, 1e-5);

		NormalNoiseModel<Character> model = normalEM.fitModel(prefs);
		double achievedLL = model.logLikelihood(prefs);
				
		ScoredItems<Character> params = model.getValueMap();

		System.out.println(params);

		assertTrue("LL didn't reach close to target", (achievedLL - targetLL)/targetLL < tol);

		for( int i = 0; i < stuff.length; i++ ) {
			assertEquals(-i*strDiff, params.get(stuff[i]).doubleValue(), tol);
		}
			
	}

}
