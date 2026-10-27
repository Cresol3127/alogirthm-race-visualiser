package algorithmrace;

import java.util.Locale;

/**
 * Prints the final ranking and search summary.
 */
final class Report {

    static final int TIMING_SIZE = 1200;
    static final int TIMING_REPEATS = 3;
    static final long TIMING_SEED = 12345L;

    static final String[] SORT_NAMES = {
        "Bubble Sort", "Insertion Sort", "Selection Sort"
    };

    private Report() {
    }

    static void printReport(double[] averages, int size, long seed,
                            int[] targets, String[] labels) {
        double[] times = measureTimes();
        int[] order = orderByTime(times);

        System.out.println(Text.paint(Text.BOLD + Text.CYAN, "FINAL RESULTS"));
        System.out.println(Text.paint(Text.DIM, "-------------"));
        System.out.println(Text.paint(Text.DIM,
                "Array size: " + size + "   seed: " + seed));
        System.out.println();

        for (int position = 0; position < order.length; position++) {
            int id = order[position];
            String rank = String.format(Locale.US, "%-3s",
                    rankLabel(position + 1));
            String name = String.format(Locale.US, "%-16s",
                    SORT_NAMES[id]);
            String time = String.format(Locale.US, "%8.2f ms", times[id]);

            System.out.println("  " + Text.paint(rankColor(position + 1), rank)
                    + "  " + Text.paint(Text.WHITE, name)
                    + " " + Text.paint(Text.YELLOW, time));
        }

        System.out.println();
        System.out.println(Text.paint(Text.DIM,
                "Best of " + TIMING_REPEATS + " runs on n="
                        + TIMING_SIZE + "."));

        System.out.println();
        System.out.println(Text.paint(Text.BOLD + Text.CYAN,
                "SEARCH TARGETS"));
        System.out.println(Text.paint(Text.DIM, "--------------"));
        System.out.println(Text.paint(Text.DIM,
                "In each round, all three search algorithms target "
                        + "the same value"));
        System.out.println(Text.paint(Text.DIM,
                "in the sorted array."));
        System.out.println();

        for (int i = 0; i < targets.length; i++) {
            String value = String.format(Locale.US, "%-4d", targets[i]);
            System.out.println("  " + Text.paint(Text.DIM, "Target")
                    + " " + Text.paint(Text.YELLOW, value)
                    + " " + Text.paint(Text.WHITE, labels[i]));
        }

        System.out.println();
        System.out.println(Text.paint(Text.BOLD + Text.CYAN,
                "AVERAGE PROBES PER KEY"));
        System.out.println(Text.paint(Text.DIM, "----------------------"));
        printAverage("Linear Search", averages[0]);
        printAverage("Binary Search", averages[1]);
        printAverage("Jump Search", averages[2]);

        System.out.println();
        System.out.println(Race.checksPassed
                ? Text.paint(Text.BOLD + Text.GREEN,
                        "VALIDATION: ALL CHECKS PASSED")
                : Text.paint(Text.BOLD + Text.RED,
                        "VALIDATION: SOME CHECKS FAILED"));
    }

    static double[] measureTimes() {
        int[] base = Race.makeData(TIMING_SIZE, TIMING_SEED);
        double[] times = new double[SORT_NAMES.length];

        for (int id = 0; id < SORT_NAMES.length; id++) {
            long best = Long.MAX_VALUE;

            for (int repeat = 0; repeat < TIMING_REPEATS; repeat++) {
                int[] data = base.clone();
                Race.beginCountingOnly();

                long start = System.nanoTime();
                Race.runSort(id, data);
                long elapsed = System.nanoTime() - start;

                if (elapsed < best) {
                    best = elapsed;
                }
                if (!Race.isSorted(data)) {
                    Race.checksPassed = false;
                }
            }

            times[id] = best / 1_000_000.0;
        }
        return times;
    }

    static int[] orderByTime(double[] times) {
        int[] order = new int[times.length];

        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }

        for (int i = 0; i < order.length; i++) {
            for (int j = i + 1; j < order.length; j++) {
                if (times[order[j]] < times[order[i]]) {
                    int temp = order[i];
                    order[i] = order[j];
                    order[j] = temp;
                }
            }
        }
        return order;
    }

    static void printAverage(String name, double value) {
        String label = String.format(Locale.US, "%-15s", name);
        String average = String.format(Locale.US, "%6.2f", value);

        System.out.println("  " + Text.paint(Text.WHITE, label)
                + " " + Text.paint(Text.YELLOW, average));
    }

    static String rankLabel(int rank) {
        switch (rank) {
            case 1: return "1st";
            case 2: return "2nd";
            case 3: return "3rd";
            default: return rank + "th";
        }
    }

    static String rankColor(int rank) {
        switch (rank) {
            case 1: return Text.BOLD + Text.YELLOW;
            case 2: return Text.CYAN;
            case 3: return Text.WHITE;
            default: return Text.DIM;
        }
    }
}
