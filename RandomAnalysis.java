import java.util.ArrayList;
import java.util.Random;
import java.math.BigInteger;

public class RandomAnalysis {

    public static final int N = 25;
    public static long a = 1425562079;
    public static long b = 1033575927;
    public static long m = 1536159409;
    public static long previous;

    public static void main(final String[] theArgs) {
        System.out.println("\n\nConstants Selected by Random Prime Generator:\n" +
                           "       a = " + a + "   b = " + b + "   m = " + m + "\n\n");
        seed(10);
        BigInteger[] pRandNums = new BigInteger[N];// the psuedo-random number stream

        for (int i = 0; i < N; i++) {
            pRandNums[i] = BigInteger.valueOf(rand());
            System.out.println("Random#" + (i+1) + " = " + pRandNums[i].toString());
        }
        System.out.println("\n\nThe values for q_(n+1):");
        BigInteger[] qn1s = new BigInteger[N];
        showQs(pRandNums, qn1s);

        System.out.println("\n\nThe values for t_n:");
        BigInteger[] tns = new BigInteger[N];
        showTs(qn1s, tns);

        System.out.println("\n\nCalculated values for x_i = a*x_(i-1) + b modm: ");

        BigInteger hackedM = getGCD(tns);
        System.out.println("M value is:  " + hackedM);

        BigInteger hackedA = findA(pRandNums[0], pRandNums[1], pRandNums[2], hackedM);
        System.out.println("A value is:  " + hackedA);

        BigInteger hackedB = findB(pRandNums[0], pRandNums[1], hackedA, hackedM);
        System.out.println("B value is:  " + hackedB);
    }

    /**Find 'b' the only unknown left
    x2 = a*x1 + b modm    ->    b = x2 - a*x1 modm  */
    public static BigInteger findB(BigInteger x1, BigInteger x2, BigInteger a, BigInteger bigM) {
      BigInteger numBuild = a.multiply(x1);//a*x1
      numBuild = x2.subtract(numBuild);//move to other side of equation
      numBuild = numBuild.mod(bigM);// apply Z_m
      return numBuild;
    }
    /**Find 'a' by solving a system of linear congruences which were dervived
    from the recurrence relation
    x2 = a*x1 + b modm
    x3 = a*x2 + b modm */
    public static BigInteger findA(BigInteger x1, BigInteger x2, BigInteger x3, BigInteger bigM) {
      BigInteger numBuild = x1.subtract(x2);
      numBuild = numBuild.modInverse(bigM);
      numBuild = numBuild.multiply(x2.subtract(x3));
      numBuild = numBuild.mod(bigM);
      return numBuild;
    }
    /**The gcd of theTs is with high probability the value of m*/
    public static BigInteger getGCD(BigInteger[] theTs) {
      BigInteger gcd = theTs[1].gcd(theTs[2]);// we indexed theTs starting at 1
      for(int i = 3; i < 21; i++) {// compare twenty total
        gcd = gcd.gcd(theTs[i]);
      }
      return gcd;
    }
    /**This method implements the formula
    t_n = q_(n+2)*q_n-(q_(n-1))^2
    then shows that the result is congruent to 0modm */
    public static void showTs(BigInteger[] theQn1s, BigInteger[] theTs) {
      BigInteger qn;
      BigInteger qn1;
      BigInteger qn2;//reserve some vars so we don't trash our data
      BigInteger numBuild;
      BigInteger bigM = BigInteger.valueOf(m);
      for(int i = 2; i < N - 3; i++) {//calculate 20 values for tn
        qn = theQn1s[i-1];
        qn1 = theQn1s[i];
        qn2 = theQn1s[i+1];
        numBuild = qn2.multiply(qn);// qn2*qn
        qn1 = qn1.multiply(qn1);//now squared
        numBuild = numBuild.subtract(qn1);//subtract qn1^2
        theTs[i-1] = numBuild; // save tn at index n
        numBuild = numBuild.mod(bigM);// verify congruent to 0modm

        System.out.println("n = "+ i + "  t_n = "+ theTs[i-1].toString() + "   t_n%m = " + numBuild.toString());

      }
    }

    /**This method fills an array of the all values
    Q_(n+1) = X_(n+1) - X_n modm
    note* I am not quite sure why we are allowed to use 'm' here
    it seems like we have imported the solution into our work.
    */
    public static void showQs(BigInteger[] xStream, BigInteger[] theQn1s) {
      BigInteger bigM = BigInteger.valueOf(m);
      for(int i = 0; i < N - 1; i++) {
        theQn1s[i] = xStream[i+1].subtract(xStream[i]).mod(bigM);
        if(theQn1s[i].compareTo(BigInteger.ZERO) < 0) {
          theQn1s[i].add(bigM); // no negative modulo
        }
        if (i != 0)// because the first value is garbage, we won't print it
          System.out.println("n = "+ i + "  q_n+1 = "+ theQn1s[i].toString());

      }

    }

    ////////////////////Initialization////////////////////
    //Initialize the seed.
    public static void seed(final long seed) {
        previous = (int) seed;
    }

    //Generate random number.
    public static long rand () {

        previous = (a * previous + b) % m;
        return previous;
    }

    //Generate random 32 bits.
    public static long random23Bits() {
        StringBuilder s = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < 32; i++) {
            s.append(r.nextInt(2));
        }
        return Long.parseUnsignedLong(s.toString(), 2);
    }
}
