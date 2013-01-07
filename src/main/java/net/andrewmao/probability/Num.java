package net.andrewmao.probability;

/*
 * Originally from 
 * http://www.iro.umontreal.ca/~simardr/ssj/indexe.html
 */

public class Num {

	/**
	 * The value of <SPAN CLASS="MATH">(2)<SUP>1/2</SUP></SPAN>.
	 * 
	 */
	public static final double RAC2 = 1.41421356237309504880;

	/**
	 * Evaluates a series of Chebyshev polynomials <SPAN CLASS="MATH"><I>T</I><SUB>j</SUB></SPAN> at
	 *   <SPAN CLASS="MATH"><I>x</I></SPAN> over the basic interval <SPAN CLASS="MATH">[- 1, &nbsp;1]</SPAN>. It uses
	 *    the method of Clenshaw, i.e., computes and  returns
	 *   
	 * <P></P>
	 * <DIV ALIGN="CENTER" CLASS="mathdisplay">
	 * <I>y</I> = <IMG
	 *  ALIGN="MIDDLE" BORDER="0" SRC="Numimg4.png"
	 *  ALT="$\displaystyle {\frac{{a_0}}{2}}$"> + &sum;<SUB>j=1</SUB><SUP>n</SUP><I>a</I><SUB>j</SUB><I>T</I><SUB>j</SUB>(<I>x</I>).
	 * </DIV><P></P>
	 * @param a coefficients of the polynomials
	 * 
	 *        @param n largest degree of polynomials
	 * 
	 *        @param x the parameter of the <SPAN CLASS="MATH"><I>T</I><SUB>j</SUB></SPAN> functions
	 * 
	 *        @return  the value of a series of Chebyshev polynomials <SPAN CLASS="MATH"><I>T</I><SUB>j</SUB></SPAN>.
	 * 
	 */
	public static double evalCheby (double a[], int n, double x)  {
		if (Math.abs (x) > 1.0)
			System.err.println ("Chebychev polynomial evaluated "+
					"at x outside [-1, 1]");
		final double xx = 2.0*x;
		double b0 = 0.0;
		double b1 = 0.0;
		double b2 = 0.0;
		for (int j = n; j >= 0; j--) {
			b2 = b1;
			b1 = b0;
			b0 = (xx*b1 - b2) + a[j];
		}
		return (b0 - b2)/2.0;
	}
}
