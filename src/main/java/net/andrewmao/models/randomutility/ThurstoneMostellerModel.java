package net.andrewmao.models.randomutility;

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

public class ThurstoneMostellerModel<T> extends RandomUtilityModel<T> {
		
	private RealMatrix mat;
	
	private TMLogLikelihood logLKfunc;
	private PowellOptimizer optim;
	
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
		
		optim = new PowellOptimizer(1e-7, 1e-11); // From commons 2.2 defaults
		
		logLKfunc = new TMLogLikelihood(mat);
	}
		
	public void addData(T winner, T loser, int count) {
		int winIdx = items.indexOf(winner);
		int loseIdx = items.indexOf(loser);
		
		mat.setEntry(winIdx, loseIdx, mat.getEntry(winIdx, loseIdx) + count);		
	}

	@Override
	public double[] getParameters() {
		double[] start = new double[items.size() - 1];
		Arrays.fill(start, 0.0);
		
		// Due to Optimization exception for default of 100
		PointValuePair result = optim.optimize(500, logLKfunc, GoalType.MINIMIZE, start);
				
		RealVector strEst = new ArrayRealVector(new double[] {0.0}, result.getPoint());
		
		return strEst.toArray();		
	}

}
