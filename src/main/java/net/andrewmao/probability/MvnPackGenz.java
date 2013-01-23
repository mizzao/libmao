package net.andrewmao.probability;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;

public interface MvnPackGenz extends Library {

	MvnPackGenz lib = (MvnPackGenz) Native.loadLibrary(
			MvnPackGenz.class.getClassLoader().getResource("mvnpack.so").getPath(), MvnPackGenz.class);
	
	void mvndst(IntByReference n, double[] lower, double[] upper, int[] infin, double[] correl,
			IntByReference maxpts, DoubleByReference abseps, DoubleByReference releps, 
			DoubleByReference error, DoubleByReference value, IntByReference inform);
	
	void mvnexp(IntByReference n, double[] lower, double[] upper, int[] infin, double[] correl,
			IntByReference maxpts, DoubleByReference abseps, DoubleByReference releps,
			DoubleByReference error, double[] value, IntByReference inform);

}
