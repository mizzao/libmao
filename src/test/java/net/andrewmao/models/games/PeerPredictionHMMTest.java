package net.andrewmao.models.games;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.Observation;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import be.ac.ulg.montefiore.run.jahmm.toolbox.KullbackLeiblerDistanceCalculator;
import be.ac.ulg.montefiore.run.jahmm.toolbox.MarkovGenerator;

public class PeerPredictionHMMTest {

	int sequences = 1000;
	int seqLength = 200;
	
	int iters = 50;
	
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
		
		BaumWelchLearner bwl = new BaumWelchLearner();
		
		Hmm<SigActObservation<CandySignal, CandyReport>> learntHmm = getInitHmm();
		
		// This object measures the distance between two HMMs
		KullbackLeiblerDistanceCalculator klc = 
			new KullbackLeiblerDistanceCalculator();
		
		// Incrementally improve the solution
		for (int i = 0; i < iters; i++) {
			System.out.println("Distance at iteration " + i + ": " +
					klc.distance(learntHmm, origHmm));
			learntHmm = bwl.iterate(learntHmm, generatedSeqs);
		}
		
		System.out.println("Resulting HMM:\n" + learntHmm);
		
		
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
