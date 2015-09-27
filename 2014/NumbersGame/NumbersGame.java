import java.util.*;
import java.io.*;
import java.math.BigInteger;

public class NumbersGame {
    public static final boolean DEBUG = false;
    public static final BigInteger ONE = BigInteger.ONE;
    public static final BigInteger TWO = BigInteger.valueOf(2);
    public static void debug(String line) {
        if(DEBUG) System.err.println(line);
    }
    public static void debug(String fmt, Object... args) {
        if(DEBUG) System.err.printf(fmt, args);
    }

    public static void main(String args[]) throws Exception {
        NumbersGame solver = new NumbersGame(args.length > 0 ? args[0] : null);
        solver.solve();
    }

    private Scanner solution;
    private Scanner input = new Scanner(System.in);
    private List<BigInteger> numbers = new ArrayList<>();
    private List<BigInteger> diffs = new ArrayList<>();
    public NumbersGame(String sol) throws Exception {
        if(sol != null)
            solution = new Scanner(new File(sol));
        HIST.add(TWO);
    }

    public void verify(int i, List<BigInteger> numbers, String decision) throws Exception {
        if(solution == null) return;
        String expected = solution.next();
        if(!expected.equals(decision))
            debug("[%02d] %s, expected %s but %s\n", i+1, numbers, expected, decision);
    }

    public void solve() throws Exception {
        int N = input.nextInt();
        for(int i = 0; i < N; i++) {
            BigInteger n1 = input.nextBigInteger();
            BigInteger n2 = input.nextBigInteger(); BigInteger n3 = input.nextBigInteger();
            numbers.add(n1);
            numbers.add(n2);
            numbers.add(n3);
            Collections.sort(numbers);
            String decision = doSolve(numbers.get(0), numbers.get(1), numbers.get(2));
            if(DEBUG)
                verify(i, numbers, decision);
            else
                System.out.println(decision);
            numbers.clear();
            diffs.clear();
        }

    }

    private List<BigInteger> HIST = new ArrayList<>();

    private boolean isBaseCase(BigInteger diff1, BigInteger diff2) {
        return diff1.compareTo(TWO) < 0 &&  diff2.compareTo(TWO) < 0;
    }

    private String solveBaseCase(BigInteger diff1, BigInteger diff2) {
        // 0, 0 => 2nd
        // 0, 1 => 1st
        // 1, 0 => 1st 
        //          1,2,2
        //          -> 2,2,2 2nd
        //          -> 1,1,2 1st -> 1,1,1 2nd
        // 1, 1 => 2nd 
        //          1,2,3
        //          -> 1,1,2 1st -> 1,1,1 2nd
        //          -> 2,2,3 1st -> 2,2,2 2nd
        debug("solving base case for (%s, %s)\n", diff1, diff2);
        return (diff1.add(diff2).equals(BigInteger.ONE)) ? "First" : "Second";
    }

    private boolean isBalanced(BigInteger diff1, BigInteger diff2) {
        return diff1.equals(diff2) || diff1.equals(diff2.subtract(ONE));
    }

    private boolean isFirst(BigInteger diff) {
        //  1st     2nd
        //          1
        //   2      3-5
        //   6-10   11-21
        //  22-42   43-85

        if(diff.equals(BigInteger.ONE))
            return false;

        // extend the limit if necessary
        while(true) {
            BigInteger last = HIST.get(HIST.size()-1);
            if(last.compareTo(diff) >= 0) break;
            HIST.add(last.multiply(TWO).subtract(ONE).multiply(TWO));
        }

        // locate the range for diff >= 2
        int low;
        int high = Collections.binarySearch(HIST, diff);
        debug("binary search for %s: %d\n", diff, high);
        if(high >= 0)
            return true;

        high = -high - 1;
        low = high - 1;
        if(low < 0) {
            // diff == 2
            return true;
        } else {
            BigInteger base = HIST.get(low);
            debug("found base %s for diff %s\n", base, diff);
            if(base.compareTo(TWO) == 0)
                return false;
            return diff.compareTo(base.multiply(TWO).subtract(ONE)) < 0;
       }
    }

    private String doSolve(BigInteger n1, BigInteger n2, BigInteger n3) {
        BigInteger diff1 = n2.subtract(n1);
        BigInteger diff2 = n3.subtract(n2);
        if(isBaseCase(diff1, diff2))
            return solveBaseCase(diff1, diff2);
        if(isBalanced(diff1, diff2))
            return isFirst(diff1) || isFirst(diff2) ? "First" : "Second";
        else {
            BigInteger diff3 = diff1.add(diff2);
            return isFirst(diff1) || isFirst(diff2) || isFirst(diff3) ? "First" : "Second";
        }
    }
}
