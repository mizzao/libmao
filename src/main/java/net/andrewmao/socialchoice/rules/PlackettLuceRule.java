package net.andrewmao.socialchoice.rules;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.mutable.MutableDouble;

import net.andrewmao.models.discretechoice.PlackettLuceModel;
import net.andrewmao.models.discretechoice.ScoredItems;

public class PlackettLuceRule extends ScoredVotingRule {
	
	double LARGE_SPACER = 1e6;

	@Override
	public <T> ScoredItems<T> getScoredRanking(PreferenceProfile<T> profile) {
		/*
		 * TODO: make this preprocessing more robust in the case of preference profiles > 4 
		 * possible idea: break candidates into strongly connected components of a DAG
		 */
		if( profile.getNumCandidates() > 4 ) 
			throw new RuntimeException("Proper preprocessing for larger preference profiles not supported yet.");
		
		T alwaysWinner = profile.getConstantWinner();
		T alwaysLoser = profile.getConstantLoser();
		
		if( alwaysWinner != null || alwaysLoser != null ) {
			profile = profile.preprocess();
//			System.out.println("Preprocessed profile:\n" + profile);
			
			T alwaysWinner2 = profile.getConstantWinner();
			T alwaysLoser2 = profile.getConstantLoser();			
						
			if( alwaysWinner2 != null || alwaysLoser2 != null ) {				
				ScoredItems<T> items = new ScoredItems<T>();				
				items.put(alwaysWinner, new MutableDouble(0));
				items.put(alwaysWinner2, new MutableDouble(-LARGE_SPACER));
				items.put(alwaysLoser2, new MutableDouble(-2*LARGE_SPACER));
				items.put(alwaysLoser, new MutableDouble(-3*LARGE_SPACER));
				return items;
			}
		}
		
		List<T> candidates = Arrays.asList(profile.getSortedCandidates());
		
		PlackettLuceModel<T> model = new PlackettLuceModel<T>(candidates);
		
		for( T[] ranking : profile.profile )
			model.addData(ranking);		
		
		ScoredItems<T> params = null;
		try {
			params = model.getParameters();
		}
		catch( RuntimeException e ) {
			// In this case, need to divide the profile in half and fit each piece separately
			PreferenceProfile<T> firstHalf = profile.slice(0,2);
			PreferenceProfile<T> secondHalf = profile.slice(2,4);
			
//			System.out.println(firstHalf);
//			System.out.println(secondHalf);
			
			PlackettLuceModel<T> model1 = new PlackettLuceModel<T>(Arrays.asList(firstHalf.getSortedCandidates()));
			for( T[] ranking : firstHalf.profile )
				model1.addData(ranking);	
			ScoredItems<T> firstParams = model1.getParameters();
			
			PlackettLuceModel<T> model2 = new PlackettLuceModel<T>(Arrays.asList(secondHalf.getSortedCandidates()));
			for( T[] ranking : secondHalf.profile )
				model2.addData(ranking);	
			ScoredItems<T> secondParams = model2.getParameters();
			// Put a large negative number into second params and combine
			for( MutableDouble val : secondParams.values() )
				val.add(-LARGE_SPACER);
			
			firstParams.putAll(secondParams);
			return firstParams;
		}		
		
		if( alwaysWinner != null ) 
			params.put(alwaysWinner, new MutableDouble(Double.POSITIVE_INFINITY));
		if( alwaysLoser != null ) 
			params.put(alwaysLoser, new MutableDouble(Double.NEGATIVE_INFINITY));
		
		return params;
	}

	@Override
	public String toString() {
		return "PlackettLuce";
	}
}
