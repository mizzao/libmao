package net.andrewmao.probability;

import com.sun.jna.Native;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * Direct-mapped library to mvnpack.so
 * @author mao
 *
 */
public class MvnPackDirect implements MvnPackGenz {
	
	static {
		Native.register(MvnPackDirect.class.getClassLoader().getResource("mvnpack.so").getPath());
	}

	@Override
	public native void mvndst_(IntByReference n, double[] lower, double[] upper,
			int[] infin, double[] correl, IntByReference maxpts,
			DoubleByReference abseps, DoubleByReference releps,
			DoubleByReference error, DoubleByReference value,
			IntByReference inform);

	@Override
	public native void mvnexp_(IntByReference n, double[] lower, double[] upper,
			int[] infin, double[] correl, IntByReference maxpts,
			DoubleByReference abseps, DoubleByReference releps,
			double[] errors, double[] value, IntByReference inform);

}
