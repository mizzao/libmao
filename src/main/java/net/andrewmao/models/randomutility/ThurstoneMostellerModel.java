package net.andrewmao.models.randomutility;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math.linear.MatrixVisitorException;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.direct.PowellOptimizer;
import org.apache.commons.math.optimization.general.AbstractScalarDifferentiableOptimizer;

public class ThurstoneMostellerModel<T> implements RandomUtilityModel<T> {
	
	private List<T> items;
	private RealMatrix mat;
	
	private TMLogLikelihood logLKfunc;
	private AbstractScalarDifferentiableOptimizer optim;
	
	public ThurstoneMostellerModel(List<T> items) {
		this.items = items;				
		
		mat = new Array2DRowRealMatrix(items.size(), items.size());		
		mat.walkInRowOrder(new DefaultRealMatrixChangingVisitor() {
			public double visit(int row, int column, double value)
					throws MatrixVisitorException {
				if (row == column)
					return 0;
				else
					return 0.08;
			}
		});
		
		optim = new PowellOptimizer();
		optim.setMaxIterations(500); // Due to Optimization exception for default of 100
		
		logLKfunc = new TMLogLikelihood(mat);
	}
	
	@Override
	public void addData(T winner, T loser, int count) {
		int winIdx = items.indexOf(winner);
		int loseIdx = items.indexOf(loser);
		
		mat.setEntry(winIdx, loseIdx, mat.getEntry(winIdx, loseIdx) + count);		
	}

	@Override
	public double[] getParameters() throws MathException {
		double[] start = new double[items.size() - 1];
		Arrays.fill(start, 0.0);
		
		RealPointValuePair result = optim.optimize(logLKfunc, GoalType.MINIMIZE, start);
		RealVector strEst = new ArrayRealVector(new double[] {0.0});
		strEst = strEst.append(result.getPoint());
		return strEst.getData();		
	}

}
