import java.util.*;
import java.lang.*;
import java.io.*;
import java.text.*;

class ExamScheduling {
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

    public static boolean DEBUG = false;
    public static void debug(String fmt, Object... args) {
        if(DEBUG)
            System.err.printf(fmt, args);
    }

    private Scanner mSC;
    public ExamScheduling(InputStream in) {
        mSC = new Scanner(in);
    }

    /**
     * Parses the schedule from stdin and figure out the maximum number of students available for the exam.
     */
    public int solve() {
        int[] timeslots = new int[5 * 12 * 4];
        int duration = mSC.nextInt();
        int students = mSC.nextInt();
        mSC.nextLine();
        // parses by students
        for(int i = 0; i < students; i++) {
            String schedule = mSC.nextLine();
            debug("parsing %s\n", schedule);
            String[] intervals = schedule.split(", ");
            // datetime format: "%s %d:%d-%d:%d" to parse
            for(String interval: intervals) {
                String[] fields = interval.split(" ");
                Day day = Day.parse(fields[0]).get();
                String[] times = fields[1].split("-");
                String[] hhmm = times[0].split(":");
                int hour = Integer.parseInt(hhmm[0]);
                int min = Integer.parseInt(hhmm[1]);
                int from = hour * 60 + min - 8 * 60;
                hhmm = times[1].split(":");
                hour = Integer.parseInt(hhmm[0]);
                min = Integer.parseInt(hhmm[1]);
                int to = hour * 60 + min - 8 * 60;
                int slots = (to - from) / 15;
                int start = day.ordinal() * (12 * 4) + from / 15;
                // Accumulate the timeslots in 15 mins in the schedule.
                debug("interval: %s, from: %d, to: %d, slots: %d, start: %d\n", interval, from, to, slots, start);
                for(int slot = 0; slot < slots; slot++)
                    timeslots[start + slot]++;
            }
        }

        int max = 0;
        duration = duration / 15;
        for(int day = 0; day < 5; day++) {
            for(int i = day * 12 * 4; i < (day+1) * 12 * 4 - duration; i++) {
                // Let the duration be a sliding window of timeslots.
                // The number of students available in the window is the minimum count in the timeslots.
                int min = Integer.MAX_VALUE;
                for(int j = 0; j < duration; j++)
                    min = Math.min(min, timeslots[i+j]);
                // Compare to find out the maximum number of students available in all the sliding windows.
                max = Math.max(max, min);
            }
        }

        if(DEBUG) {
            int expected = mSC.nextInt();
            if(max == expected) debug("Correct\n");
            else debug("Incorrect: %d but %d expected\n", max, expected);
        }
        return max;
    }

    public static void main (String[] args) throws java.lang.Exception {
       ExamScheduling scheduler = new ExamScheduling(System.in);
       int max = scheduler.solve();
       System.out.println(max);
    }
}
