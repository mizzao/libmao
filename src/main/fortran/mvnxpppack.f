*
* This file contains a short test program and MVNXPP, a subroutine
* for computing expected values for MVN distribution variables and
* the expected values for the squares of the distribution variables.
* This file uses MVNDST and must be compiled with MVNDST and supporting
* software. The test program demonstrates the use of MVNXPP for computing 
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
c$$$*     Test program for MVNXPP
c$$$*
c$$$      DOUBLE PRECISION ABSEPS, RELEPS
c$$$      INTEGER N, NN, I, J, K, IJ, MAXPTS, IFTK
c$$$      PARAMETER ( N = 6, NN = ((N-1)*N)/2, MAXPTS = 5000*N*N*N )
c$$$      PARAMETER ( ABSEPS = 0, RELEPS = 0.005 )
c$$$      DOUBLE PRECISION VALK(0:2*N), ERRK(0:2*N)
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
c$$$         CALL MVNXPP( N, LOW, UP, INFIN, CORREL, 
c$$$     *        MAXPTS, ABSEPS, RELEPS, ERRK, VALK, IFTK )
c$$$         PRINT '('' Results for MVNEXP, with Inform ='', I2 )', IFTK
c$$$         PRINT '(''        Values        Errors'' / (I2, 2E14.6))',
c$$$     *        ( I, VALK(I), ERRK(I), I = 0, 2*N )
c$$$      END
*
      SUBROUTINE MVNXPP( N, LOWER, UPPER, INFIN, CORREL, MAXPTS, 
     *                      ABSEPS, RELEPS, ERROR, VALUE, INFORM )
     & BIND(C, name='mvnxpp_')
*
*     A subroutine for computing expected values for MVN variables,
*      and the squares of the MVN variables.
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
*     VALUE  REAL array(0:2*N) of estimated values for the integrals
*            VALUE(0) is just the MVN value. 
*            VALUE(I) is the expected value for variable I, 0<I<=N 
*            VALUE(I) is the expected value squared variable I. N<I<=2*N
*     INFORM INTEGER, termination status parameter:
*            if INFORM = 0, normal completion with ERROR < EPS;
*            if INFORM = 1, completion with ERROR > EPS and MAXPTS 
*                           function vaules used; increase MAXPTS to 
*                           decrease ERROR;
*            if INFORM = 2, N > 100 or N < 1.
*
      EXTERNAL MVVDFN, MVVDFM
      INTEGER N, INFIN(*), MAXPTS, INFORM, IVLS, I, INFRMI, INFIS
      DOUBLE PRECISION 
     *     CORREL(*), LOWER(*), UPPER(*), RELEPS, ABSEPS, D, E,
     *     ERROR(0:*), VALUE(0:*), MVVDNT, MVVDFN, MVVDFM, MVVDNM
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
            VALUE(2) = MVVDNM( 1, 1, CORREL, LOWER, UPPER, INFIN )
            ERROR(2) = 1D-16
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
               INFRMI = MVVDNM( I, N, CORREL, LOWER, UPPER, INFIN )
               IVLS = 0
               CALL DKBVRC( N-1, IVLS, MAXPTS, MVVDFM, ABSEPS, RELEPS, 
     *              ERROR(I+N), VALUE(I+N), INFRMI )
               INFORM = MAX( INFRMI, INFORM )
            END DO
         END IF
         DO I = 1, 2*N
            ERROR(I) = ERROR(I)/VALUE(0)
            VALUE(I) = VALUE(I)/VALUE(0)
         END DO
      ENDIF
      END
*
*  End MVNXPP
*
*     
*     Andrew Mao: The functions below are already provided in mvnexppack.f
*
c$$$      DOUBLE PRECISION FUNCTION MVVDFN( N, W )
c$$$*
c$$$*     Integrand subroutine
c$$$*
c$$$      INTEGER N, INFIN(*), INFIS
c$$$      DOUBLE PRECISION W(*), LOWER(*), UPPER(*), CORREL(*), SQTWPI
c$$$      INTEGER NL, IJ, I, J, IV, II, INFRMI
c$$$      PARAMETER ( NL = 100, SQTWPI = 2.50662 82746 31001D0 )
c$$$      DOUBLE PRECISION COV((NL*(NL+1))/2), A(NL), B(NL), Y(NL)
c$$$      INTEGER INFI(NL)
c$$$      DOUBLE PRECISION PROD, AI, BI, DI, EI, SUM, PHINVS, MVVDNT, D1, E1
c$$$      SAVE D1, E1, A, B, INFI, COV
c$$$      DI = D1
c$$$      EI = E1
c$$$      PROD = EI - DI 
c$$$      IJ = 1
c$$$      DO I = 1, N
c$$$         Y(I) = PHINVS( DI + W(I)*( EI - DI ) )
c$$$         SUM = 0
c$$$         DO J = 1,I
c$$$            IJ = IJ + 1
c$$$            SUM = SUM + COV(IJ)*Y(J)
c$$$         END DO
c$$$         IJ = IJ + 1
c$$$         IF ( INFI(I+1) .NE. 0 ) AI = A(I+1) - SUM 
c$$$         IF ( INFI(I+1) .NE. 1 ) BI = B(I+1) - SUM 
c$$$         CALL MVNLMS( AI, BI, INFI(I+1), DI, EI )
c$$$         PROD = PROD*( EI - DI )
c$$$      END DO
c$$$      MVVDFN = PROD*Y(1)
c$$$      RETURN
c$$$*
c$$$*     Entry point for intialization.
c$$$*
c$$$      ENTRY MVVDNT( IV, N, CORREL, LOWER, UPPER, INFIN )
c$$$*
c$$$*     Initialization and computation of covariance Cholesky factor.
c$$$*
c$$$       I = INFIN(IV)
c$$$      AI = LOWER(IV)
c$$$      BI = UPPER(IV)
c$$$      DI = 0
c$$$      EI = 0
c$$$      IF ( I .NE. 0 ) DI = -EXP( -AI**2/2 )/SQTWPI
c$$$      IF ( I .NE. 1 ) EI = -EXP( -BI**2/2 )/SQTWPI
c$$$      IF ( N .EQ. 1 ) THEN
c$$$         MVVDNT = EI - DI
c$$$      ELSE
c$$$         MVVDNT = 0
c$$$         CALL MVNLMS( AI, BI, I, D1, E1 )
c$$$         Y(IV) = ( EI - DI )/( E1 - D1 )
c$$$         LOWER(IV) = Y(IV)
c$$$         UPPER(IV) = Y(IV)
c$$$         INFIN(IV) = 2
c$$$         CALL COVSRT( N,LOWER,UPPER,CORREL,INFIN,Y, INFIS,A,B,COV,INFI )
c$$$         LOWER(IV) = AI
c$$$         UPPER(IV) = BI
c$$$         INFIN(IV) =  I
c$$$      END IF
c$$$      END
*
      DOUBLE PRECISION FUNCTION MVVDFM( N, W )
*
*     Integrand subroutine
*
      INTEGER N, INFIN(*), INFIS
      DOUBLE PRECISION W(*), LOWER(*), UPPER(*), CORREL(*), SQTWPI
      INTEGER NL, IJ, I, J, IV, II, INFRMI
      PARAMETER ( NL = 100, SQTWPI = 2.50662 82746 31001D0 )
      DOUBLE PRECISION COV((NL*(NL+1))/2), A(NL), B(NL), Y(NL)
      INTEGER INFI(NL)
      DOUBLE PRECISION PROD, AI, BI, DI, EI, SUM, PHINVS, MVVDNM, D1, E1
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
      MVVDFM = PROD*Y(1)**2
      RETURN
*
*     Entry point for intialization.
*
      ENTRY MVVDNM( IV, N, CORREL, LOWER, UPPER, INFIN )
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
      CALL MVNLMS( AI, BI, I, D1, E1 )
      IF ( N .EQ. 1 ) THEN
         MVVDNM = BI*EI - AI*DI + E1 - D1
      ELSE
         MVVDNM = 0
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


