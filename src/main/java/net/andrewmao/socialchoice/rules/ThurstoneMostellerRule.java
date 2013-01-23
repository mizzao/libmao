package net.andrewmao.socialchoice.rules;

import net.andrewmao.models.discretechoice.ScoredItems;
import net.andrewmao.models.discretechoice.ThurstoneMostellerModel;

public class ThurstoneMostellerRule extends ScoredVotingRule {

	ThurstoneMostellerModel tm;
	boolean useAllPairs;
	
	public ThurstoneMostellerRule(boolean useAllPairs) {
		this.useAllPairs = useAllPairs;
		tm = new ThurstoneMostellerModel();
	}
	
	@Override
	public <T> ScoredItems<T> getScoredRanking(PreferenceProfile<T> profile) {				
		return tm.fitModel(profile, useAllPairs).getValueMap();								
	}	

	public String toString() { 
		return useAllPairs ? "TMAllP" : "TMAdjP"; 
	}

}
