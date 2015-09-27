import java.util.*;
import java.lang.*;
import java.io.*;

class Dominoes {
    private Scanner mSC;
    public Dominoes(InputStream in) {
        mSC = new Scanner(in);
    }

    /**
     * Compute the cascade distances in the specified orientation
     *
     * @param dominoes the positions and heights of the input dominoes
     * @param right the specified orientation
     */
    private NavigableMap<Integer, Integer> computeCascadeDistances(NavigableMap<Integer, Integer> dominoes, boolean right) {
        // Switch the dominoes' view in the orientation
        if(!right) 
            dominoes = dominoes.descendingMap();

        // For the right cascade distances, compute from right to left. 
        // For the left cascade distances, compute from left to right. 
        NavigableMap<Integer, Integer> distances = new TreeMap<>();
        for(int pos: dominoes.descendingKeySet()) {
            int h = dominoes.get(pos); // Must be > 0    
            if(distances.isEmpty()) {
                distances.put(pos, h);
            } else {
                // Right: consider pos < dominoes <= pos + h
                // Left:  consider pos - h <= dominoes < pos
                NavigableMap<Integer, Integer> cascade = right 
                    ? distances.subMap(pos, false, pos + h, true)
                    : distances.subMap(pos - h, true, pos, false);
                // Take the maximum absolute cascade distance
                int distance;
                if(right) 
                    distance = Math.abs(cascade.entrySet().stream()
                        .mapToInt(e -> e.getKey() + e.getValue())
                        .max().orElse(pos + h) - pos);
                else
                    distance = Math.abs(cascade.entrySet().stream()
                        .mapToInt(e -> e.getKey() - e.getValue())
                        .min().orElse(pos - h) - pos);
                // Take the max between the distance and its height
                distances.put(pos, Math.max(h, distance));
            }
        }
        return distances; 
    }

    /**
     * Efficiently output 0 whenever there is a gap between dominoes.
     *
     * @param distances the cascade distances of the dominoes
     * @param N the total number of dominoes
     */
    private void printDistances(NavigableMap<Integer, Integer> distances, int N) {
        int prev = -1;
        for(int pos: distances.keySet()) {
            if(pos > prev) {
                if(prev >= 0)
                    System.out.print(" ");
                for(int i = prev + 1; i < pos; i++)
                    System.out.print("0 ");
            } 
            System.out.print(distances.get(pos));
            prev = pos;
        }
    }

    public void solve() {
        // Read from stdin as is.
        int N = mSC.nextInt();
        NavigableMap<Integer, Integer> dominoes = new TreeMap<>();
        for(int i = 0; i < N; i++) {
            int dominoe = mSC.nextInt();
            if(dominoe > 0)
                dominoes.put(i, dominoe);
        }
        
        // Compute both orientations and output respectively
        NavigableMap<Integer, Integer> cascadeDR = computeCascadeDistances(dominoes, true);
        NavigableMap<Integer, Integer> cascadeDL = computeCascadeDistances(dominoes, false);
        printDistances(cascadeDR, N);
        System.out.println();
        printDistances(cascadeDL, N);
    }

    private static boolean DEBUG = Boolean.parseBoolean(System.getProperty("DEBUG", "false"));
    private static void debug(String fmt, Object... args) {
        if(DEBUG)
            System.err.printf(fmt, args);
    }

    public static void main(String[] args) {
        Dominoes solver = new Dominoes(System.in);
        solver.solve();
    }
}
