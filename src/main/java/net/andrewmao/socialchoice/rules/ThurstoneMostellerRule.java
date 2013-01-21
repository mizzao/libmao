package net.andrewmao.socialchoice.rules;

import java.util.Arrays;
import java.util.List;

import net.andrewmao.models.discretechoice.PairwiseDiscreteChoiceModel;
import net.andrewmao.models.discretechoice.ScoredItems;
import net.andrewmao.models.discretechoice.ThurstoneMostellerModel;

public class ThurstoneMostellerRule extends ScoredVotingRule {

	boolean useAllPairs;
	
	public ThurstoneMostellerRule(boolean useAllPairs) {
		this.useAllPairs = useAllPairs;
	}
	
	@Override
	public <T> ScoredItems<T> getScoredRanking(PreferenceProfile<T> profile) {
		List<T> candidates = Arrays.asList(profile.getSortedCandidates());
		
		ThurstoneMostellerModel<T> tm = new ThurstoneMostellerModel<T>(candidates);
				
		addPairs(tm, profile);
		
		return tm.getParameters();		
	}

	protected <T> void addPairs(PairwiseDiscreteChoiceModel<T> tm,
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
		return useAllPairs ? "TMAllP" : "TMAdjP"; 
	}

}
