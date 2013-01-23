package net.andrewmao.socialchoice.rules;

import net.andrewmao.models.discretechoice.BradleyTerryModel;
import net.andrewmao.models.discretechoice.ScoredItems;

public class BradleyTerryRule extends ScoredVotingRule {

	BradleyTerryModel bt;
	boolean useAllPairs;
	
	public BradleyTerryRule(boolean useAllPairs) {
		this.useAllPairs = useAllPairs;
		bt = new BradleyTerryModel();
	}
	
	@Override
	public <T> ScoredItems<T> getScoredRanking(PreferenceProfile<T> profile) {		
		return bt.fitModel(profile, useAllPairs).getValueMap();					
	}	

	public String toString() { 
		return useAllPairs ? "BTAllP" : "BTAdjP"; 
	}

}
