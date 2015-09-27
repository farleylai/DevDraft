import java.util.*;
import java.lang.*;
import java.io.*;

class MinionZapper {
    private Scanner mSC;
    public MinionZapper(InputStream in) {
        mSC = new Scanner(in);
    }

    public int solve() {
        // Read from stdin as is.
        int N = mSC.nextInt();
        int X = mSC.nextInt();
        int Y = mSC.nextInt();
        
        NavigableMap<Integer, Integer> minions = new TreeMap<>();
        List<Integer> enemies = new ArrayList<>();
        for(int i = 0; i < N; i++) {
            // Special case: cannot be killed or dead by definition.
            int minion = mSC.nextInt();
            if(minion <= 0) continue;
            enemies.add(minion);
        }
        if(enemies.isEmpty())
            return 0;
        
        // Special cases for Y < 0. Let X be the maximum possible 
        // damage and Y be its additive inverse. 
        // The problem remains.
        if(Y < 0) {
            X += -Y * (enemies.size()-1);
            Y = -Y;
        }
        for(int minion: enemies) {
            if(minion > X)
                continue;
            // Remember the least damage hit index to kill the minion.
            // Killing by this hit ensures the optimality.
            // If Y == 0, only damage X is considered.
            int yHits = Y == 0 ? 0 : (X - minion) / Y;
            int count = minions.getOrDefault(yHits, 0) + 1;
            minions.put(yHits, count);
        }

        // Compute the solution only if there are minions.
        if(minions.isEmpty())
            return 0;
        int solution = 0;
        int remaining = 0;
        int prevHits = minions.lastKey() + 1;
        // Accumulate the killing count in a descending order
        // since the minion killed by this hit can also be killed
        // by more hit damage.
        for(int yHits: minions.descendingKeySet()) {
            // Each such hit only kills one. If there are more than one,
            // they can be killed by the available hits in the least hit 
            // index gap from the previous one.
            int availableHits = prevHits - yHits - 1; // Must be >= 0
            int hits = Math.min(availableHits, remaining);
            remaining -= hits;
            prevHits = yHits;
            int count = minions.get(yHits); // Must be > 0
            // Accumulate those killed by this hit and available hits 
            // as well as those unkilled if any.
            solution += hits + 1;
            remaining += count - 1;
        }
        return solution += Math.min(remaining, minions.firstKey());
    }

    private static boolean DEBUG = Boolean.parseBoolean(System.getProperty("DEBUG", "false"));
    private static void debug(String fmt, Object... args) {
        if(DEBUG)
            System.err.printf(fmt, args);
    }

    public static void main(String[] args) {
        MinionZapper solver = new MinionZapper(System.in);
        int solution = solver.solve();
        System.out.println(solution);
    }
}
