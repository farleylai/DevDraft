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
        int lines = input.nextInt();
        int dest = input.nextInt();
        int transfers = input.nextInt();
        List<Train> trains = new ArrayList<>();
        for(int i = 0; i < lines; i++) {
            int[] stops = new int[input.nextInt()];
            for(int s = 0; s < stops.length; s++)
                stops[s] = input.nextInt();
            int[] fares = new int[stops.length - 1];
            for(int s = 0; s < stops.length - 1; s++)
                fares[s] = input.nextInt();
            trains.add(new Train(stops, fares));
        }

        TripPlanner solver = new TripPlanner(trains, transfers);
        int ret = solver.solve();
        
        if(DEBUG) {
            int solution = input.nextInt();
            if(solution != ret) 
                debug("computed %d but solution is %d\n", ret, solution);
            else
                debug("computed %d correctly\n", ret);
        } else
            System.out.println(ret);
    }

    public static class Train {
        int [] mStops;
        int [] mFares;

        public Train(int[] stops, int[] fares) {
            mStops = stops;
            mFares = fares;
        }
        
        public boolean contains(int stop) {
            return Arrays.binarySearch(mStops, stop) >= 0;
        }

        public int next(int stop) {
            int pos = Arrays.binarySearch(mStops, stop);
            if(pos < 0) return -1;
            return pos == mStops.length - 1 ? -1 : mStops[pos + 1];
        }

        public int fare(int from, int to) {
            if(!contains(from) || !contains(to))
                return Integer.MAX_VALUE;
            
            int fromPos = Arrays.binarySearch(mStops, from);
            int toPos = Arrays.binarySearch(mStops, to);
            int fare = 0;
            for(int i = fromPos; i < toPos; i++)
                fare += mFares[i];
            return fare;
        }
    }

    public static class TripPlanner {
        private static class Route {
            private Train mTrain;
            private Route mPrev;
            private int mStops;
            private int mStop;
            private int mFare;
            private int mTransfers;

            public Route() {
            }

            private Route(Train train, int stop, int fare, Route prev, boolean transfered) {
                mTrain = train;
                mStop = stop;
                mFare = fare;
                mPrev = prev;
                mStops = prev.mStops + 1;
                mTransfers = prev.mTransfers + (transfered ? 1 : 0);
            }

            public int stop() {
                return mStop;
            }

            public int stops() {
                return mStops;
            }

            public int transfers() {
                return mTransfers;
            }

            public int fare() {
                return mFare;
            }
            
            /**
             * Take the train to the next stop and return the new route if possible.
             * 
             * @param the train to take to the next stop
             * @return the new route to the next stop by the train or null if impossible
             */
            public Route take(Train train) {
                int stop = train.next(mStop);
                if(stop < 0) return null;
                int fare = mFare + train.fare(mStop, stop);
                debug("route %d->%d at fare %d\n", mStop, stop, fare);
                return new Route(train, stop, fare, this, mTrain != null && mTrain != train);
            }

            @Override
            public String toString() {
                List<Route> routes = new ArrayList<>();
                Route r = this;
                while(r.mPrev != null) {
                    routes.add(r);
                    r = r.mPrev;
                }
                routes.add(r);
                Collections.reverse(routes);
                if(routes.isEmpty()) return "";
                StringBuilder builder = new StringBuilder();
                builder.append(routes.get(0).stop());
                for(int i = 1; i < routes.size(); i++)
                    builder.append("->").append(routes.get(i).stop());
                builder.append(" at fare ").append(fare())
                    .append(" of ").append(routes.size())
                    .append(" stops within ").append(mTransfers)
                    .append(" transfers");
                return builder.toString();
            }
        }

        private List<Train> mTrains;
        private Route mBest;
        private final int TRANSFERS;
        public TripPlanner(List<Train> trains, int transfers) {
            mTrains = trains;
            TRANSFERS = transfers;
        }

        /**
         * Compete the current best route in terms of the fares and route length.
         *
         * @param route the route to compare
         * @return true if better or false otherwise
         */
        public boolean isBetter(Route route) {
            return mBest == null 
                ? true 
                : route.fare() < mBest.fare() 
                        ? true 
                        : route.fare() == mBest.fare() && (route.transfers() < mBest.transfers() || route.stops() < mBest.stops())
                            ? true 
                            : false;
        }

        public boolean isAllowed(Route route) {
            return route.transfers() <= TRANSFERS;
        }

        /**
         * Find the route at the lowest fare.
         *
         * @return the lowest fare
         */
        public int solve() {
            Stack<Route> trace = new Stack<>();
            trace.push(new Route());
            while(!trace.isEmpty()) {
                Route r = trace.pop();
                for(Train train: mTrains) {
                    if(!train.contains(r.stop())) 
                        continue;

                    Route next = r.take(train);
                    if(next != null) {
                        if(isAllowed(next) && isBetter(next)) 
                            trace.push(next);
                    } else if(isBetter(r))
                        mBest = r;
                }
            }
            
            if(DEBUG)
                debug("Best route: %s\n", mBest);
            return mBest.fare();
        }
    }
}
