package edu.harvard.econcs.voting.rules;

import java.util.List;

import net.andrewmao.models.discretechoice.ScoredItems;

public abstract class ScoredVotingRule implements VotingRule {
	
	public String toString() { return this.getClass().getSimpleName(); }
	
	public abstract <T> ScoredItems<T> getScoredRanking(PreferenceProfile<T> profile);		
	
	public <T> List<T> getRanking(PreferenceProfile<T> preferences) {				
		return getScoredRanking(preferences).getRanking();				
	}

}
