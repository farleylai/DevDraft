import java.util.*;
import java.io.*;

public class Main {
    public static final boolean DEBUG = !true;

    public static void debug(String line) {
        if(DEBUG) System.err.println(line);
    }
    public static void debug(String fmt, Object... args) {
        if(DEBUG) System.err.printf(fmt, args);
    }

    public static void main(String args[]) throws Exception {
        Scanner input = new Scanner(System.in);
        int D = input.nextInt();
        int entries = input.nextInt();
        MatchMaker matcher = new MatchMaker(D);
        for(int i = 0; i < entries; i++) {
            String event = input.next();
            int id = input.nextInt();
            if(event.equals("logon")) {
                int Elo = input.nextInt();
                matcher.logon(id, Elo);
                debug("entry: %s %d %d\n", event, id, Elo);
            } else if(event.equals("logoff")) {
                matcher.logoff(id);
                debug("entry: %s %d\n", event, id);
            }
        }
        
        if(DEBUG) {
            int games = input.nextInt();
            int Elos = input.nextInt();
            int waiters = input.nextInt();
            if(games != matcher.games() || Elos != matcher.Elos() || waiters != matcher.waiters()) {
                debug("Computed (games: %d, Elos: %d, waiters: %d) but expected (%d, %d, %d)\n", matcher.games(), matcher.Elos(), matcher.waiters(), games, Elos, waiters);
                debug(matcher.toString());
            } else
                debug("%d %d %d, correct!\n", matcher.games(), matcher.Elos(), matcher.waiters());
        } else
            System.out.printf("%d %d %d\n", matcher.games(), matcher.Elos(), matcher.waiters());
    }

    public static class MatchMaker {
        public static class Player implements Comparable<Player> {
            private int mId;
            private int mElo;

            public Player(int id, int Elo) {
                mId = id;
                mElo = Elo;
            }

            public int id() { return mId; }
            public int Elo() { return mElo; }

            public int diff(Player p) {
                return Math.abs(mElo - p.Elo());
            }

            @Override
            public int compareTo(Player p) {
                int ret = Integer.compare(mElo, p.Elo());
                return ret == 0 ? Integer.compare(mId, p.id()) : ret;
            }

            @Override
            public String toString() {
                return String.format("<%d, %d>", mId, mElo);
            }
        }

        public static class Game {
            private Player mP1;
            private Player mP2;
            public Game(Player p1, Player p2) {
                mP1 = p1;
                mP2 = p2;
            }

            public int diff() {
                return mP1.diff(mP2);
            }

            @Override
            public String toString() {
                return String.format("%s vs. %s, diff: %d", mP1, mP2, diff());
            }
        }

        private NavigableSet<Player> mPool;
        private Set<Game> mGames;
        private final int D;

        public MatchMaker(int D) {
            mPool = new TreeSet<>();
            mGames = new HashSet<>();
            this.D = D;
        }

        public MatchMaker logon(int id, int Elo) {
            Player player = new Player(id, Elo);
            if(mPool.isEmpty())
                mPool.add(player);
            else {
                // find the opponent if possible
                Player opponent = mPool.ceiling(player);
                Player floor = mPool.floor(player);
                if(opponent == null)
                    opponent = floor;
                if(floor == null)
                    floor = opponent;
                int D1 = player.diff(opponent);
                int D2 = player.diff(floor);
                if(D1 <= D2 && D1 <= D)
                    match(player, opponent);
                else if(D1 > D2 && D2 <= D)
                    match(player, floor);
                else
                    mPool.add(player);
            }
            return this;
        }

        public MatchMaker logoff(int id) {
            // remove player of id in the pool, no harm to games
            for(Iterator<Player> itr = mPool.iterator(); itr.hasNext();) {
                Player p = itr.next();
                if(p.id() == id)
                    itr.remove();
            }
            return this;
        }

        public MatchMaker match(Player player, Player opponent) {
            mPool.remove(opponent);
            mGames.add(new Game(player, opponent));
            return this;
        }

        public int Elos() {
            int Elos = 0;
            for(Game g: mGames)
                Elos += g.diff();
            return Elos;
        }

        public int games() {
            return mGames.size();
        }

        public int waiters() {
            return mPool.size();
        }

        @Override
        public String toString() {
            return String.format("Games: \n%s\nPool: \n%s\n", mGames, mPool);
        }
    }
}
