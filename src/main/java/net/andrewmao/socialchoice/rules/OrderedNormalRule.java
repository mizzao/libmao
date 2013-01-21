package net.andrewmao.socialchoice.rules;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;

import net.andrewmao.models.discretechoice.OrderedNormalMCEM;
import net.andrewmao.models.discretechoice.ScoredItems;

public class OrderedNormalRule extends ScoredVotingRule {

	static final int MAX_ITERS = 30;
	static final double ABS_EPS = 1e-4; // Only want order, don't care too much about actual scores
	static final double REL_EPS = 1e-5;
	
	@Override
	public <T> ScoredItems<T> getScoredRanking(PreferenceProfile<T> profile) {
		List<T> candidates = Arrays.asList(profile.getSortedCandidates());
		
		OrderedNormalMCEM<T> model = new OrderedNormalMCEM<T>(candidates);
		
		for( T[] ranking : profile.profile )
			model.addData(ranking);
		
		model.setup(new NormalDistribution(0,1).sample(4), MAX_ITERS, ABS_EPS, REL_EPS);						
		
		return model.getParameters();
	}

	@Override
	public String toString() {
		return "OrderedNormal";
	}
}
