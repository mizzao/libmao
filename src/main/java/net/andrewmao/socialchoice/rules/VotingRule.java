package net.andrewmao.socialchoice.rules;

import java.util.List;

public interface VotingRule {

	public abstract <T> List<T> getRanking(PreferenceProfile<T> profile);
	
}
