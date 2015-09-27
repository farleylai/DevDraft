import java.util.*;
import java.lang.*;
import java.io.*;

class InputScraper {
    // Recognized days of week from Mon. to Fri.
    public enum Day {
        Monday, Tuesday, Wednesday, Thursday, Friday;

        /**
         * Parse the day of week and return an optional in case.
         *
         * @param day the input string to parse
         * @return the parsing result
         */
        public static Optional<Day> parse(String day) {
            if(day == null || day.length() == 0)
                return Optional.empty();
            day = day.trim();
            for(Day d: values()) {
               if(d.name().startsWith(day)) 
                   return Optional.of(d);
            }
            return Optional.empty();
        }
    }

    /**
     * The inveral represents the free period from hh1:mm1 to hh2:mm2 in one day.
     */
    private class Interval implements Comparable<Interval> {
        private Day mDay;
        private int mStartHour;
        private int mStartMin;
        private int mEndHour;
        private int mEndMin;
        public Interval(Day day, int startHour, int startMin, int endHour, int endMin) {
            mDay = day;
            mStartHour = startHour >= 8 && startHour <= 20 ? startHour : startHour + 12;
            mStartMin = startMin;
            mEndHour = endHour >= 8 && endHour <= 20 ? endHour : endHour + 12;
            mEndMin = endMin;
        }

        /**
         * Implements Comparable for sorting.
         */
        @Override
        public int compareTo(Interval o) {
            return Integer.compare(mDay.ordinal(), o.mDay.ordinal());
        }

        /**
         * Output string format.
         */
        @Override
        public String toString() {
            return String.format("%s %d:%02d-%d:%02d", mDay.name(), mStartHour, mStartMin, mEndHour, mEndMin);
        }
    }

    public static boolean DEBUG = false;
    public static void debug(String fmt, Object... args) {
        if(DEBUG)
            System.err.printf(fmt, args);
    }

    /**
     * Determines if a given string is a positive integer.
     *
     * @param str the input string
     */
    public static boolean isPositive(String str) {
        try {  
            return Integer.parseInt(str) > 0;  
        } catch(NumberFormatException e) {  
            return false;  
        }  
    }

    /**
     * Removes unused trailing symbols depending on the input token type.
     *
     * @param token the input string token
     */
    public static String trim(String token) {
        if(Character.isDigit(token.charAt(0))) {
            // number
            for(int i = 1; i < token.length(); i++) {
                if(!Character.isDigit(token.charAt(i)))
                    return token.substring(0, i);
            }
        } else {
            // .,s
            while(token.endsWith(".") || token.endsWith(",") || token.endsWith("s"))
                token = token.substring(0, token.length() - 1);
        }
        return token;
    }

    private Scanner mSC;
    private List<Interval> mSchedule;
    public InputScraper(InputStream in) {
        mSC = new Scanner(in);
        mSchedule = new ArrayList<>();
    }

    /**
     * Parses the schedule from stdin.
     */
    public String solve() {
        int lines = mSC.nextInt();
        mSC.nextLine();
        // parses by lines
        for(int i = 0; i < lines; i++) {
            String line = mSC.nextLine();
            debug("parsing %s\n", line);
            String[] tokens = line.split(" ");
            List<Day> days = new ArrayList<>();
            List<Integer> hours = new ArrayList<>();
            List<Integer> mins = new ArrayList<>();
            // parses by tokens
            for(String token: tokens) {
                if(token.indexOf("-") > -1) {
                    // two times
                    String[] times = token.split("-");
                    for(String time: times) {
                        if(time.indexOf(":") > -1) {
                            // one time of hh:mm
                            String[] hhmm = time.split(":");
                            hours.add(Integer.parseInt(hhmm[0]));
                            mins.add(Integer.parseInt(trim(hhmm[1])));
                        } else {
                            // just hour
                            time = trim(time);
                            hours.add(Integer.parseInt(time));
                            mins.add(0);
                        }
                    }
                } else if(token.indexOf(":") > -1) {
                    // one time of hh:mm
                    String[] hhmm = token.split(":");
                    hours.add(Integer.parseInt(hhmm[0]));
                    mins.add(Integer.parseInt(trim(hhmm[1])));
                } else {
                    token = trim(token);
                    if(isPositive(token)){
                        // just hour
                        hours.add(Integer.parseInt(token));
                        mins.add(0);
                    } else {
                        // possible day of week
                        Optional<Day> day = Day.parse(token);
                        if(day.isPresent()) {
                            days.add(day.get());
                        } else if(token.toLowerCase().equals("noon")) {
                            hours.add(12);
                            mins.add(0);
                        }
                    }
                }
            }

            // Make a schedule for each day
            for(Day day: days)
                mSchedule.add(new Interval(day, hours.get(0), mins.get(0), hours.get(1), mins.get(1)));
        }

        // Output the schedule in one line
        Collections.sort(mSchedule);
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < mSchedule.size(); i++) 
            builder.append(mSchedule.get(i).toString()).append(i < mSchedule.size() - 1 ? ", " : "");
        String solution = builder.toString();
        if(DEBUG) 
            debug(mSC.nextLine().equals(solution) ? "Correct\n" : "Incorrect\n");
        return solution;
    }

    public static void main (String[] args) throws java.lang.Exception {
       InputScraper scraper = new InputScraper(System.in);
       String output = scraper.solve();
       System.out.println(output);
    }
}
