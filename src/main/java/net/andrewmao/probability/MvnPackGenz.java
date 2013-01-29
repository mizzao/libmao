package net.andrewmao.probability;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * Interface-mapped library to mvnpack.so
 * @author mao
 *
 */
public interface MvnPackGenz extends Library {

	MvnPackGenz lib = (MvnPackGenz) Native.synchronizedLibrary((Library) Native.loadLibrary(
			MvnPackGenz.class.getClassLoader().getResource("mvnpack.so").getPath(), MvnPackGenz.class));
	
	void mvndst_(IntByReference n, double[] lower, double[] upper, int[] infin, double[] correl,
			IntByReference maxpts, DoubleByReference abseps, DoubleByReference releps, 
			DoubleByReference error, DoubleByReference value, IntByReference inform);
	
	void mvnexp_(IntByReference n, double[] lower, double[] upper, int[] infin, double[] correl,
			IntByReference maxpts, DoubleByReference abseps, DoubleByReference releps,
			double[] errors, double[] value, IntByReference inform);

}
