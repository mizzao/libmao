package net.andrewmao.socialchoice.rules;

import java.util.Map;

import net.andrewmao.models.discretechoice.ScoredItems;

import org.apache.commons.lang.mutable.MutableDouble;

public abstract class PositionalVotingRule extends ScoredVotingRule {

	protected abstract double[] getPositionalScores(int length);
	
	@Override
	public <T> ScoredItems<T> getScoredRanking(PreferenceProfile<T> preferences) {				
		double[] pos = getPositionalScores(preferences.getNumCandidates());
		
		ScoredItems<T> scored = new ScoredItems<T>(preferences.getSortedCandidates());		
		
		for( T[] pref : preferences.profile ) {
			for( int i = 0; i < pref.length; i++ ) {
				scored.get(pref[i]).add(pos[i]);
			}
		}
		
		return scored;
	}
	
	public <T> ScoredItems<T> getNormalizedScores(PreferenceProfile<T> preferences) {
		Map<T, MutableDouble> scores = getScoredRanking(preferences);		
						
		ScoredItems<T> normalized = new ScoredItems<T>(scores.keySet());
		int normalizationFactor = preferences.getNumRankings();
		
		for( Map.Entry<T, MutableDouble> e : scores.entrySet() )
			normalized.get(e.getKey()).setValue(e.getValue().doubleValue() / normalizationFactor);
		
		return normalized;
	}

	public <T> void prettyPrint(PreferenceProfile<T> prefs, SocialChoiceMetric<T> m) {	
		ScoredItems<T> scores = getNormalizedScores(prefs);
		
		for(Map.Entry<T, MutableDouble> e : scores.entrySet())
			System.out.print(String.format("%d=%.4f, ", e.getKey(), e.getValue().doubleValue()));
				
		if( m != null ) System.out.println(m.computeByScore(scores) + " mistakes.");				
	}	
	
}
