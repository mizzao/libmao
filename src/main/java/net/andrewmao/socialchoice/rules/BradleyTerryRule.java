package net.andrewmao.socialchoice.rules;

import java.util.Arrays;
import java.util.List;

import net.andrewmao.models.discretechoice.BradleyTerryModel;
import net.andrewmao.models.discretechoice.PairwiseDiscreteChoiceEstimator;
import net.andrewmao.models.discretechoice.ScoredItems;

public class BradleyTerryRule extends ScoredVotingRule {

	boolean useAllPairs;
	
	public BradleyTerryRule(boolean useAllPairs) {
		this.useAllPairs = useAllPairs;
	}
	
	@Override
	public <T> ScoredItems<T> getScoredRanking(PreferenceProfile<T> profile) {
		List<T> candidates = Arrays.asList(profile.getSortedCandidates());
		
		BradleyTerryModel<T> tm = new BradleyTerryModel<T>(candidates);
				
		addPairs(tm, profile);
		
		return tm.getParameters();		
	}

	protected <T> void addPairs(PairwiseDiscreteChoiceEstimator<T> tm,
			PreferenceProfile<T> profile) {
		if( useAllPairs ) {
			// Add in all pairwise wins for each ranking
			for( T[] ranking : profile.profile ) {
				tm.addAllPairs(ranking);
			}
		}
		else {
			// Add only adjacent pairwise wins for each ranking
			for( T[] ranking : profile.profile ) {
				tm.addAdjacentPairs(ranking);			
			}
		}
	}

	public String toString() { 
		return useAllPairs ? "BTAllP" : "BTAdjP"; 
	}

}
