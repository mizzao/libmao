package net.andrewmao.models.discretechoice;

import static org.junit.Assert.*;

import net.andrewmao.socialchoice.rules.PreferenceProfile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PlackettLuceTest {
		
	PreferenceProfile<Integer> mmFailPrefs = new PreferenceProfile<Integer>(new Integer[][] {
			{1, 2, 3, 4},
			{1, 2, 4, 3},
			{2, 1, 3, 4},	
	});	

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected=RuntimeException.class)
	public void testMMFailure() {
		PlackettLuceModel plmm = new PlackettLuceModel(true);
		
		plmm.fitModel(mmFailPrefs);	
		
		fail();
	}
	
	@Test
	public void testMMApprox() {
		PlackettLuceModel plmm = new PlackettLuceModel(false);
		plmm.fitModel(mmFailPrefs);	
	}

}
