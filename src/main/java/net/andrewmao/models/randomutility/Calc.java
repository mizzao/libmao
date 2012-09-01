package net.andrewmao.models.randomutility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math.linear.InvalidMatrixException;
import org.apache.commons.math.linear.LUDecompositionImpl;
import org.apache.commons.math.linear.MatrixIndexException;
import org.apache.commons.math.linear.MatrixVisitorException;
import org.apache.commons.math.linear.QRDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.direct.PowellOptimizer;
import org.apache.commons.math.optimization.general.AbstractScalarDifferentiableOptimizer;

public class Calc {

	private int numPics;
	
	RealMatrix mat;

	private AbstractScalarDifferentiableOptimizer optim;
	
	private TMLogLikelihood logLKfunc;	
	
	private boolean alreadyDone = false;
	
	public Calc(){
	}
	
	/**
	 * Initialize vote
	 */
	public void initializeVote(int num) {
		this.numPics = num;

		mat = new Array2DRowRealMatrix(numPics, numPics);
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
		logLKfunc = new TMLogLikelihood(mat);
	}

	public boolean alreadyDone(String expId, int index){
		File file = new File(expId+"/pair"+index+".ser");
		if (!new File(expId).exists())
			new File(expId).mkdir();
		alreadyDone = file.exists();
		return alreadyDone;
	}

	/**
	 * Update vote
	 */
	public void updateVote(int item1, int item2, boolean leftChosen) {
		if (leftChosen)
			mat.setEntry(item1, item2, mat.getEntry(item1, item2) + 1);
		else
			mat.setEntry(item2, item1, mat.getEntry(item2, item1) + 1);
	}

	/**
	 * Get next pair
	 */
	public int[] getNextPair() throws IllegalArgumentException,	InvalidMatrixException, MathException {
	
		// Compute next pair
		double[] start = new double[numPics - 1];
		Arrays.fill(start, 0.0);
		RealPointValuePair result = optim.optimize(logLKfunc, GoalType.MINIMIZE, start);
		RealVector strEst = new ArrayRealVector(result.getPointRef());
		RealMatrix covar = new QRDecompositionImpl(logLKfunc.hessian(strEst)).getSolver().getInverse();
		RealMatrix prob = getCurrentProbEst(strEst, covar);
		RealMatrix infos = getInfoGains(strEst, covar);
		double[] best = getExpInfoGain(prob, infos);
		int[] currPair = new int[] { (int) best[0], (int) best[1] };
		
		return currPair;
	}

	
	
	/**
	 * Get next pair
	 */
	public int[] getNextPairAdaptive(String expId, int index) throws IllegalArgumentException,	InvalidMatrixException, MathException {
	
		File file = new File(expId+"/pair"+index+".ser");
		if (alreadyDone){
			try {
				ObjectInputStream readPair = new ObjectInputStream(
						new FileInputStream(file));
				int[] pair = (int[]) readPair.readObject();
				readPair.close();
				return pair;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		// Compute next pair
		double[] start = new double[numPics - 1];
		Arrays.fill(start, 0.0);
		RealPointValuePair result = optim.optimize(logLKfunc, GoalType.MINIMIZE, start);
		RealVector strEst = new ArrayRealVector(result.getPointRef());
		RealMatrix covar = new QRDecompositionImpl(logLKfunc.hessian(strEst)).getSolver().getInverse();
		RealMatrix prob = getCurrentProbEst(strEst, covar);
		RealMatrix infos = getInfoGains(strEst, covar);
		double[] best = getExpInfoGain(prob, infos);
		int[] currPair = new int[] { (int) best[0], (int) best[1] };
		
		// Save information to file
		try {
			
			ObjectOutputStream outVec = new ObjectOutputStream(
					new FileOutputStream(file));
			outVec.writeObject(currPair);
			outVec.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return currPair;
	}
	
	/**
	 * Get estimated strength parameters
	 */
	public double[] getStrEst() throws OptimizationException, 
			FunctionEvaluationException, IllegalArgumentException{
		double[] start = new double[numPics - 1];
		Arrays.fill(start, 0.0);
		RealPointValuePair result = optim.optimize(logLKfunc, GoalType.MINIMIZE, start);
		RealVector strEst = new ArrayRealVector(new double[] {0.0});
		strEst = strEst.append(result.getPoint());
		return strEst.getData();
	}

	/**
	 * Log raw hit information
	 */
	public void logResults(String expId, String message){
		if (alreadyDone)
			return;
		String filename = expId + "-results.txt";
		File logFile = new File(filename);
		try {
			logFile.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
			writer.append(message);
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Log list of HITS created for random experiment
	 */
	public void logHITIDS(String expId, String[] hitIDs){
		String filename = expId + "-createdHITs.txt";
		File logFile = new File(filename);
		try {
			logFile.delete();
			logFile.createNewFile();

			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
			
			for (String str : hitIDs){
				writer.append(str);
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read list of HITS created for random experiment
	 */
	public String readHITIDS(String expId){
		String filename = expId + "-createdHITs.txt";
		File logFile = new File(filename);
		String hitIDString = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(logFile));
			String line = null;
			while (( line = reader.readLine()) != null){
				hitIDString += line + " ";
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (hitIDString == "")
			return hitIDString;
		hitIDString = hitIDString.substring(0, hitIDString.length() - 1);
		return hitIDString;
	}
	
	/**
	 * Write hit index
	 * @param i
	 */
	public void writeHitIndex(int i, String expId){
		File file = new File(expId + "-hitIndex.txt");
		BufferedWriter writer;
		try {
			file.createNewFile();
			writer = new BufferedWriter(new FileWriter(file));
			String index = Integer.toString(i);
			writer.write(index);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Read hit index
	 * @return
	 */
	public int readHitIndex(String expId){
		String filename = expId + "-hitIndex.txt";
		BufferedReader reader;
		int index = -1;
		try {
			reader = new BufferedReader(new FileReader(filename));
			String line = reader.readLine();
			index = Integer.parseInt(line);
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return index;
	}

	/**
	 * Write hit ID
	 * @param hitId
	 */
	public void writeHitID(String hitId, String expId){
		File file = new File(expId + "-hitID.txt");
		BufferedWriter writer;
		try {
			file.createNewFile();
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(hitId);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Read hitId
	 * @return
	 */
	public String readHitID(String expId){
		String filename = expId + "-hitID.txt";
		BufferedReader reader;
		String hitId = "";
		try {
			reader = new BufferedReader(new FileReader(filename));
			hitId = reader.readLine();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return hitId;
	}
	
	/**
	 * Save current matrix to file
	 */
	public void writeMatrix(String expId) {
		File file = new File(expId + "-matrix.txt");
		try {
			file.createNewFile();
			ObjectOutputStream stream = new ObjectOutputStream(
					new FileOutputStream(file));
			stream.writeObject(this.mat);
			stream.flush();
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Restore matrix in from file
	 */
	public void readMatrix(String expId){
		File file = new File(expId + "-matrix.txt");
		try {
			ObjectInputStream stream = new ObjectInputStream(
					new FileInputStream(file));
			this.mat = (Array2DRowRealMatrix) stream.readObject();
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Log hit info in csv format
	 */
	public void logCSV(String expId, String message){
		if (alreadyDone)
			return;
		try {
			String filename = expId + "-rawdata.csv";
			File logFile = new File(filename);
			logFile.createNewFile();
			boolean writeHeader = false;
			
			BufferedReader reader = new BufferedReader(
					new FileReader(filename));
			if (reader.readLine() == null){
				writeHeader = true;
			}
			reader.close();

			BufferedWriter logWriter = new BufferedWriter(
					new FileWriter(filename, true));
			if (writeHeader) {
				// writer header of csv file
				logWriter.append("hitId,assignmentId,workerId,workerIndex," +
						"dotsInLeft,dotsInRight,letterLeft,letterRight,numLeft,numRight," +
						"pictureChosen,pictureCorrect,percentageChosen," +
						"gender,age,country,education,income,comments");
				logWriter.newLine();		
			}
			logWriter.append(message);
			logWriter.newLine();
			logWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get current probability estimates based on strength estimates and
	 * covariance estimates
	 */
	private RealMatrix getCurrentProbEst(RealVector strEst, RealMatrix covar)
			throws MatrixIndexException, MathException {

		int actualLength = strEst.getDimension() + 1;
		RealMatrix prob = new Array2DRowRealMatrix(actualLength, actualLength);

		RealVector strNew = new ArrayRealVector(new double[] { 0.0 });
		strNew = strNew.append(strEst);

		RealMatrix covarNew = new Array2DRowRealMatrix(actualLength,
				actualLength);
		covarNew.setSubMatrix(covar.getData(), 1, 1);

		for (int i = 0; i < actualLength; i++) {
			for (int j = 0; j < actualLength; j++) {
				if (i == j) {
					prob.setEntry(i, j, 0);
					continue;
				}
				double sd = Math
						.sqrt(1 + covarNew.getEntry(i, i)
								+ covarNew.getEntry(j, j) - 2
								* covarNew.getEntry(i, j));
				NormalDistribution normdist = new NormalDistributionImpl(0, sd);
				double probValue = normdist.cumulativeProbability(strNew
						.getEntry(i) - strNew.getEntry(j));
				prob.setEntry(i, j, probValue);
			}
		}
		return prob;
	}

	/**
	 * Compute information gain for each pair (i,j) 
	 */
	private RealMatrix getInfoGains(RealVector strEst, RealMatrix covar)
			throws IllegalArgumentException, InvalidMatrixException,
			MathException {
		RealMatrix infos = new Array2DRowRealMatrix(numPics, numPics);
		double[] start = new double[numPics - 1];
		Arrays.fill(start, 0.0);

		for (int i = 0; i < numPics; i++) {
			for (int j = 0; j < numPics; j++) {
				if (i == j)
					continue;
				double matij = mat.getEntry(i, j);
				mat.setEntry(i, j, matij + 1);
				RealPointValuePair resultij = optim.optimize(logLKfunc,
						GoalType.MINIMIZE, start);
				RealVector estij = new ArrayRealVector(resultij.getPointRef());
				RealMatrix covarij = new QRDecompositionImpl(
						logLKfunc.hessian(estij)).getSolver().getInverse();
				
				// Expected information gain is with respect to the new estimate
				double dij = compKL(estij, covarij,strEst, covar);
				infos.setEntry(i, j, dij);
				mat.setEntry(i, j, matij);
			}
		}
		return infos;
	}

	/**
	 * Compute expected information gain for each pair (i,j), i < j
	 */
	private double[] getExpInfoGain(RealMatrix prob, RealMatrix infos) {
		List<double[]> scoreList = new ArrayList<double[]>();
		RealMatrix expInfoGain = new Array2DRowRealMatrix(numPics, numPics);

		for (int i = 0; i < numPics; i++) {
			for (int j = i + 1; j < numPics; j++) {
				double currInfoGain = prob.getEntry(i, j)
						* infos.getEntry(i, j) + prob.getEntry(j, i)
						* infos.getEntry(j, i);
				
				// Add to list for sorting and returning
				scoreList.add(new double[] { i, j, currInfoGain });
				
				// Add to matrix for logging
				DecimalFormat df = new DecimalFormat("#.###");
				String formatted = df.format(currInfoGain);
				double ig = Double.parseDouble(formatted);
				expInfoGain.setEntry(i, j, ig);
				expInfoGain.setEntry(j, i, ig);
			}
		}
		
		Collections.sort(scoreList, new Comparator<double[]>() {
			public int compare(double[] arg0, double[] arg1) {
				return Double.compare(arg1[2], arg0[2]);
			}
		});

//		// Use random tie-breaking rule
//		int factor = 10000;
//		int index = 0;
//		long first = (long)Math.floor(scoreList.get(0)[2]*factor);
//		for (int i = 1; i < scoreList.size();i++){
//		    long curr = (long)Math.floor(scoreList.get(i)[2]*factor);
//			if (curr != first){
//				index = i;
//				break;
//			}
//		}
//		index = new Random().nextInt(index);
		
		return scoreList.get(0);
	}
	
	/**
	 * Compute the KL divergence of two multivariate normal distributions
	 * This is implemented based on wikipedia article
	 * http://en.wikipedia.org/wiki/Multivariate_normal_distribution#cite_note-4
	 */
	private double compKL(RealVector strEst, RealMatrix covar,
			RealVector estij, RealMatrix covarij) {
		
		RealMatrix covarijinv = new QRDecompositionImpl(covarij).getSolver().getInverse();

		double term1 = covarijinv.multiply(covar).getTrace();

		RealVector vecDiff = strEst.subtract(estij);
		double term2 = covarijinv.preMultiply(vecDiff).dotProduct(vecDiff);

		double det = new LUDecompositionImpl(covar).getDeterminant();
		double detij = new LUDecompositionImpl(covarij).getDeterminant();
		double term3 = Math.log(detij / det);

		return 0.5 * (term1 + term2 + term3 - strEst.getDimension());
	}

//	private static String now() {
//		String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
//		Calendar cal = Calendar.getInstance();
//		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
//		return sdf.format(cal.getTime());
//	}

//	private void printMatrix(String msg, RealMatrix matrix){
//		System.out.println(msg + matrixToString(matrix.getData()));
//	}
//
//	private void printVector(String msg, RealVector vector){
//		System.out.println(msg + Arrays.toString(vector.getData()));
//	}
//
//	private String listToString(List<double[]> list){
//		StringBuffer sb = new StringBuffer();
//		
//		for (double[] curr : list){
//			sb.append(Arrays.toString(curr));
//			sb.append("\n");
//		}
//		return sb.toString();
//	}
	
//	private String matrixToString(double[][] data) {
//		StringBuffer sb = new StringBuffer();
//
//		sb.append("[");
//		for (double[] row : data) {
//			for (double val : row) {
//				sb.append(val);
//				sb.append("\t");
//			}
//			sb.append(";\n");
//		}
//		sb.append("]");
//		return sb.toString();
//	}

}
