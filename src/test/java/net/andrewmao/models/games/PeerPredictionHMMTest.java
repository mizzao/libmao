package net.andrewmao.models.games;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.Observation;
import be.ac.ulg.montefiore.run.jahmm.Opdf;
import be.ac.ulg.montefiore.run.jahmm.toolbox.MarkovGenerator;

public class PeerPredictionHMMTest {

	int sequences = 1000;
	int seqLength = 200;
	
	double tol = 0.02;
	
	enum CandySignal {
		MM, GB
	}
	
	enum CandyReport {
		MM, GB
	}
	
	@Test
	public void test() {
		
		Hmm<SigActObservation<CandySignal, CandyReport>> origHmm = getThreeStateHmm();
		
		System.out.println("Starting HMM:\n" + origHmm);
		
		List<List<SigActObservation<CandySignal, CandyReport>>> generatedSeqs = 
				generateSequences(origHmm, sequences, seqLength);
		
		BWToleranceLearner bwl = new BWToleranceLearner();
		
		Hmm<SigActObservation<CandySignal, CandyReport>> learntHmm = getInitHmm();		
		
		// Incrementally improve the solution
		learntHmm = bwl.learn(origHmm, generatedSeqs);		
		
		System.out.println("Resulting HMM:\n" + learntHmm);		
		
		// Check pi
		for( int i = 0; i < 3; i++ )
			assertEquals(origHmm.getPi(i), learntHmm.getPi(i), tol);
		
		// Check a
		for( int i = 0; i < 3; i++ ) {
			for( int j = 0; j < 3; j++ ) {
				assertEquals(origHmm.getAij(i,j), learntHmm.getAij(i,j), tol);
			}
		}
				
		// Check strategy
		for( int i = 0; i < 3; i++ ) {
			Opdf<SigActObservation<CandySignal, CandyReport>> origStrategy = origHmm.getOpdf(i);
			Opdf<SigActObservation<CandySignal, CandyReport>> learnedStrategy = learntHmm.getOpdf(i);
			
			for( CandySignal cs : CandySignal.values() ) {
				for( CandyReport ca : CandyReport.values() ) {
					SigActObservation<CandySignal, CandyReport> sa = new SigActObservation<CandySignal, CandyReport>(cs, ca);
					assertEquals(origStrategy.probability(sa), learnedStrategy.probability(sa), tol);
				}
			}
		}		
		
	}

	static <O extends Observation> List<List<O>> generateSequences(Hmm<O> hmm, int n, int len)
	{
		MarkovGenerator<O> mg = new MarkovGenerator<O>(hmm);
		
		List<List<O>> sequences = new ArrayList<List<O>>();
		for (int i = 0; i < n; i++)
			sequences.add(mg.observationSequence(len));

		return sequences;
	}
	
	/**
	 * A fictional hmm with people starting mostly honest and 
	 * then gradually transitioning to MM and GB eqs.
	 * @return
	 */
	static Hmm<SigActObservation<CandySignal, CandyReport>> getThreeStateHmm() {
		
		double[] pi = new double[] { 0.8, 0.1, 0.1 };
		
		double prHonestToUninf = 0.1;
		double prStayHonest = 1d-2*prHonestToUninf;
		
		double prBecomeHonest = 0.05;
		double prStayUninf = 0.9;
		double prBecomeOpposite = 0.05;
		
		double[][] a = new double[][] {
			new double[] { prStayHonest, prHonestToUninf, prHonestToUninf },
			new double[] { prBecomeHonest, prStayUninf, prBecomeOpposite },
			new double[] { prBecomeHonest, prBecomeOpposite, prStayUninf },
		};
		
		double[][] honestProbs = new double[][] {
			new double [] { 0.9, 0.1 },
			new double [] { 0.1, 0.9 }
		};
		
		double[][] mmProbs = new double[][] {
			new double [] { 0.9, 0.1 },
			new double [] { 0.9, 0.1 }
		};
		
		double[][] gbProbs = new double[][] {
			new double [] { 0.1, 0.9 },
			new double [] { 0.1, 0.9 }
		};
		
		@SuppressWarnings("unchecked")
		List<OpdfStrategy<CandySignal, CandyReport>> opdfs 
		= Lists.newArrayList(
				getOpdf(honestProbs),
				getOpdf(mmProbs),
				getOpdf(gbProbs)
				);
		
		return new Hmm<SigActObservation<CandySignal, CandyReport>>(pi, a, opdfs);
	}

static Hmm<SigActObservation<CandySignal, CandyReport>> getInitHmm() {
	
		double oneThird = 1d/3;
	
		double[] pi = new double[] { oneThird, oneThird, oneThird };
		
		double[][] a = new double[][] {
			new double[] { oneThird, oneThird, oneThird },
			new double[] { oneThird, oneThird, oneThird },
			new double[] { oneThird, oneThird, oneThird },
		};
		
		double[][] honestProbs = new double[][] {
			new double [] { 0.6, 0.4 },
			new double [] { 0.4, 0.6 }
		};
		
		double[][] mmProbs = new double[][] {
			new double [] { 0.6, 0.4 },
			new double [] { 0.6, 0.4 }
		};
		
		double[][] gbProbs = new double[][] {
			new double [] { 0.4, 0.6 },
			new double [] { 0.4, 0.6 }
		};
		
		@SuppressWarnings("unchecked")
		List<OpdfStrategy<CandySignal, CandyReport>> opdfs 
		= Lists.newArrayList(
				getOpdf(honestProbs),
				getOpdf(mmProbs),
				getOpdf(gbProbs)
				);
		
		return new Hmm<SigActObservation<CandySignal, CandyReport>>(pi, a, opdfs);
	}

	// Use an even prior to fit shit
	static double[] signalPrior = new double[] { 0.5, 0.5 };

	private static OpdfStrategy<CandySignal, CandyReport> getOpdf(double[][] probs) {		
		return new OpdfStrategy<CandySignal, CandyReport>(
				CandySignal.class, CandyReport.class, signalPrior, probs);
	}
}
