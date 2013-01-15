package net.andrewmao.models.discretechoice;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.direct.PowellOptimizer;

public class ThurstoneMostellerModel<T> extends PairwiseDiscreteChoiceModel<T> {
		
	private RealMatrix mat;
	
	private TMNLogLikelihood nll;
	
	PowellOptimizer optim;
//	NonLinearConjugateGradientOptimizer optim, backup;
	
	public ThurstoneMostellerModel(List<T> items) {
		super(items);			
		
		mat = new Array2DRowRealMatrix(items.size(), items.size());		
		mat.walkInRowOrder(new DefaultRealMatrixChangingVisitor() {
			public double visit(int row, int column, double value) {
				if (row == column)
					return 0;
				else
					return 0.08;
			}
		});
		 
		// TODO: switch to gradient optimizer (why is it broken?)
		optim = new PowellOptimizer(1e-7, 1e-11); // From commons 2.2 defaults
		
//		optim = new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE);
//		backup = new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.FLETCHER_REEVES);
		
		nll = new TMNLogLikelihood(mat);
	}
	
	@Override
	public void addData(T winner, T loser, int count) {
		int winIdx = items.indexOf(winner);
		int loseIdx = items.indexOf(loser);
		
		mat.setEntry(winIdx, loseIdx, mat.getEntry(winIdx, loseIdx) + count);		
	}

	@Override
	public ScoredItems<T> getParameters() {
		double[] start = new double[items.size() - 1];
		Arrays.fill(start, 0.0);
		
		PointValuePair result = optim.optimize(1000, nll, GoalType.MINIMIZE, start);			
		
		RealVector strEst = new ArrayRealVector(new double[] {0.0}, result.getPoint());
				
		return new ScoredItems<T>(items, strEst.toArray());
	}

}
