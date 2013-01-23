package net.andrewmao.socialchoice.rules;

import org.apache.commons.math3.distribution.NormalDistribution;

import net.andrewmao.models.discretechoice.OrderedNormalMCEM;
import net.andrewmao.models.discretechoice.ScoredItems;

public class OrderedNormalRule extends ScoredVotingRule {

	static final int MAX_ITERS = 30;
	static final double ABS_EPS = 1e-4; // Only want order, don't care too much about actual scores
	static final double REL_EPS = 1e-5;
	
	@Override
	public <T> ScoredItems<T> getScoredRanking(PreferenceProfile<T> profile) {				
		OrderedNormalMCEM model = new OrderedNormalMCEM();		
		
		model.setup(new NormalDistribution(0,1).sample(4), MAX_ITERS, ABS_EPS, REL_EPS);						
		
		return model.fitModel(profile).getValueMap();
	}

	@Override
	public String toString() {
		return "OrderedNormal";
	}
}
