*
* This file contains a short test program and MVNEXP, a subroutine
* for computing expected values for MVN distribution variables. This 
* file uses MVNDST and must be compiled with MVNDST and supporting
* software. The test program demonstrates the use of MVNEXP for computing 
* MVN expected values for a six dimensional example problem.
*
*          Alan Genz
*          Department of Mathematics
*          Washington State University
*          Pullman, WA 99164-3113
*          Email : alangenz@wsu.edu
*
c$$$      PROGRAM TSTMVN
c$$$*
c$$$*     Test program for MVNEXP
c$$$*
c$$$      DOUBLE PRECISION ABSEPS, RELEPS
c$$$      INTEGER N, NN, I, J, K, IJ, MAXPTS, IFTK
c$$$      PARAMETER ( N = 6, NN = ((N-1)*N)/2, MAXPTS = 5000*N*N*N )
c$$$      PARAMETER ( ABSEPS = 0, RELEPS = 0.005 )
c$$$      DOUBLE PRECISION VALK(0:N), ERRK(0:N)
c$$$      DOUBLE PRECISION CORREL(NN), LOW(N), UP(N)
c$$$      INTEGER INFIN(N)
c$$$*          Evans/Swartz Problem, N = 6
c$$$      DATA (CORREL(I),I=1,NN)/ -0.86557994439447D0, -0.76453948395932D0,
c$$$     &   0.5D0, -0.73085933048094D0, 2*0.5D0, -0.71401925374174D0,
c$$$     & 3*0.5D0, -0.70391520769823D0, 4*0.5D0/
c$$$*
c$$$      PRINT '(''                  Test of MVNEXP'')'
c$$$      PRINT '(12X, ''Requested Accuracy '',F8.5)', MAX(ABSEPS,RELEPS)
c$$$      PRINT '(''           Number of Dimensions is '',I2)', N
c$$$      PRINT '(''     Maximum # of Function Values is '',I7)', MAXPTS
c$$$*
c$$$         PRINT '('' I     Limits'')'
c$$$         PRINT '(4X,''Lower  Upper  Lower Left of Correlation Matrix'')'
c$$$         IJ = 0
c$$$         DO I = 1, N
c$$$            LOW(I) = -1D0/I
c$$$            UP(I)  =  1D0/I
c$$$            INFIN(I) = MOD( I, 3 )
c$$$            IF ( INFIN(I) .EQ. 0 ) THEN
c$$$               PRINT '(I2, '' -infty'', F7.4, 1X, 7F9.5)',
c$$$     *              I, UP(I), ( CORREL(IJ+J), J = 1,I-1 ), 1.0
c$$$            ELSE IF ( INFIN(I) .EQ. 1 ) THEN
c$$$               PRINT '(I2, F7.4, ''  infty '', 7F9.5)',
c$$$     *              I, LOW(I), ( CORREL(IJ+J), J = 1,I-1 ), 1.0
c$$$            ELSE
c$$$               PRINT '(I2, 2F7.4, 1X, 7F9.5)',
c$$$     *              I, LOW(I), UP(I), ( CORREL(IJ+J), J = 1,I-1 ), 1.0
c$$$            ENDIF
c$$$            IJ = IJ + I-1
c$$$         END DO
c$$$         CALL MVNEXP( N, LOW, UP, INFIN, CORREL, 
c$$$     *        MAXPTS, ABSEPS, RELEPS, ERRK, VALK, IFTK )
c$$$         PRINT '('' Results for MVNEXP, with Inform ='', I2 )', IFTK
c$$$         PRINT '(''        Values        Errors'' / (I2, 2E14.6))',
c$$$     *        ( I, VALK(I), ERRK(I), I = 0, N )
c$$$      END
*
      SUBROUTINE MVNEXP( N, LOWER, UPPER, INFIN, CORREL, MAXPTS, 
     *                      ABSEPS, RELEPS, ERROR, VALUE, INFORM )
     & BIND(C, name='mvnexp_')
*
*     A subroutine for computing expected values for MVN variables.
*     This subroutine uses an algorithm given in the paper
*     "Numerical Computation of Multivariate Normal Probabilities", in
*     J. of Computational and Graphical Stat., 1(1992), pp. 141-149, by
*          Alan Genz 
*          Department of Mathematics
*          Washington State University 
*          Pullman, WA 99164-3113
*          Email : alangenz@wsu.edu
*
*  Parameters
*
*     N      INTEGER, the number of variables.
*     LOWER  REAL, array of lower integration limits.
*     UPPER  REAL, array of upper integration limits.
*     INFIN  INTEGER, array of integration limits flags:
*            if INFIN(I) = 0, Ith limits are (-infinity, UPPER(I)];
*            if INFIN(I) = 1, Ith limits are [LOWER(I), infinity);
*            if INFIN(I) = 2, Ith limits are [LOWER(I), UPPER(I)].
*     CORREL REAL, array of correlation coefficients; the correlation
*            coefficient in row I column J of the correlation matrix
*            should be stored in CORREL( J + ((I-2)*(I-1))/2 ), for J < I.
*     NF     INTEGER, number of functions for expected values.
*     MAXPTS INTEGER, maximum number of function values allowed. This 
*            parameter can be used to limit the time. A sensible 
*            strategy is to start with MAXPTS = 1000*N, and then
*            increase MAXPTS if ERROR is too large.
*     ABSEPS    REAL absolute error tolerance.
*     RELEPS    REAL relative error tolerance.
*     ERROR  REAL array(0:N) of estimated abs errors, with 99% confidence.
*            ERROR(I) is estimated error for VALUE(I).
*     VALUE  REAL array(0:N) of estimated values for the integrals
*            VALUE(0) is just the MVN value. 
*            VALUE(I) is the expected value for variable I. 
*     INFORM INTEGER, termination status parameter:
*            if INFORM = 0, normal completion with ERROR < EPS;
*            if INFORM = 1, completion with ERROR > EPS and MAXPTS 
*                           function vaules used; increase MAXPTS to 
*                           decrease ERROR;
*            if INFORM = 2, N > 100 or N < 1.
*
      EXTERNAL MVVDFN
      INTEGER N, INFIN(*), MAXPTS, INFORM, IVLS, I, INFRMI, INFIS
      DOUBLE PRECISION 
     *     CORREL(*), LOWER(*), UPPER(*), RELEPS, ABSEPS,
     *     ERROR(0:*), VALUE(0:*), MVVDNT, D, E, MVVDFN
      IF ( N .GT. 100 .OR. N .LT. 1 ) THEN
         INFORM = 2
         DO I = 0, N
            VALUE(I) = 0
            ERROR(I) = 1
         END DO
      ELSE
         CALL MVNDST( N, LOWER, UPPER, INFIN, CORREL, MAXPTS,
     *                ABSEPS, RELEPS, ERROR(0), VALUE(0), INFORM )
         IF ( N .EQ. 1 ) THEN
            VALUE(1) = MVVDNT( 1, 1, CORREL, LOWER, UPPER, INFIN )
            ERROR(1) = 1D-16
         ELSE
            DO I = 1, N
               INFRMI = MVVDNT( I, N, CORREL, LOWER, UPPER, INFIN )
*     
*              Call the lattice rule integration subroutine
*
               IVLS = 0
               CALL DKBVRC( N-1, IVLS, MAXPTS, MVVDFN, ABSEPS, RELEPS, 
     *              ERROR(I), VALUE(I), INFRMI )
               INFORM = MAX( INFRMI, INFORM )
            END DO
         END IF
         DO I = 1, N
            ERROR(I) = ERROR(I)/VALUE(0)
            VALUE(I) = VALUE(I)/VALUE(0)
         END DO
      ENDIF
      END
*
      DOUBLE PRECISION FUNCTION MVVDFN( N, W )
*
*     Integrand subroutine
*
      INTEGER N, INFIN(*), INFIS
      DOUBLE PRECISION W(*), LOWER(*), UPPER(*), CORREL(*), SQTWPI
      INTEGER NL, IJ, I, J, IV, II, INFRMI
      PARAMETER ( NL = 100, SQTWPI = 2.50662 82746 31001D0 )
      DOUBLE PRECISION COV((NL*(NL+1))/2), A(NL), B(NL), Y(NL)
      INTEGER INFI(NL)
      DOUBLE PRECISION PROD, AI, BI, DI, EI, SUM, PHINVS, MVVDNT, D1, E1
      SAVE D1, E1, A, B, INFI, COV
      DI = D1
      EI = E1
      PROD = EI - DI 
      IJ = 1
      DO I = 1, N
         Y(I) = PHINVS( DI + W(I)*( EI - DI ) )
         SUM = 0
         DO J = 1,I
            IJ = IJ + 1
            SUM = SUM + COV(IJ)*Y(J)
         END DO
         IJ = IJ + 1
         IF ( INFI(I+1) .NE. 0 ) AI = A(I+1) - SUM 
         IF ( INFI(I+1) .NE. 1 ) BI = B(I+1) - SUM 
         CALL MVNLMS( AI, BI, INFI(I+1), DI, EI )
         PROD = PROD*( EI - DI )
      END DO
      MVVDFN = PROD*Y(1)
      RETURN
*
*     Entry point for intialization.
*
      ENTRY MVVDNT( IV, N, CORREL, LOWER, UPPER, INFIN )
*
*     Initialization and computation of covariance Cholesky factor.
*
       I = INFIN(IV)
      AI = LOWER(IV)
      BI = UPPER(IV)
      DI = 0
      EI = 0
      IF ( I .NE. 0 ) DI = -EXP( -AI**2/2 )/SQTWPI
      IF ( I .NE. 1 ) EI = -EXP( -BI**2/2 )/SQTWPI
      IF ( N .EQ. 1 ) THEN
         MVVDNT = EI - DI
      ELSE
         MVVDNT = 0
         CALL MVNLMS( AI, BI, I, D1, E1 )
         Y(IV) = ( EI - DI )/( E1 - D1 )
         LOWER(IV) = Y(IV)
         UPPER(IV) = Y(IV)
         INFIN(IV) = 2
         CALL COVSRT( N,LOWER,UPPER,CORREL,INFIN,Y, INFIS,A,B,COV,INFI )
         LOWER(IV) = AI
         UPPER(IV) = BI
         INFIN(IV) =  I
      END IF
      END


