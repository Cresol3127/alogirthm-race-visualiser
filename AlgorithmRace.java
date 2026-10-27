package algorithmrace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Algorithm Race
 * --------------
 * A simple ASCII visualiser that races three sorting algorithms on the same
 * data, then races three search algorithms on the sorted result.
 *
 * Compile: javac algorithmrace/*.java
 * Run:     java algorithmrace.AlgorithmRace
 */
public class AlgorithmRace {

    static final int MIN_SIZE = 4;
    static final int MAX_SIZE = 30;
    static final int DEFAULT_SIZE = 24;
    static final long DEFAULT_SEED = 42;

    private AlgorithmRace() {
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        printIntro();

        Integer size = readSize(scanner);
        if (size == null) {
            scanner.close();
            return;
        }

        Long seed = readSeed(scanner);
        if (seed == null) {
            scanner.close();
            return;
        }

        boolean ok = playRound(size, seed);
        scanner.close();

        if (!ok) {
            System.exit(1);
        }
    }

    static boolean playRound(int size, long seed) {
        Race.checksPassed = true;

        int[] data = Race.makeData(size, seed);
        int[] sorted = data.clone();
        Arrays.sort(sorted);
        int maxValue = sorted[sorted.length - 1];

        List<Race.Run> sortRuns = Race.recordSorts(data, sorted);

        int[] targets = {
            sorted[size / 2],
            sorted[size - 1] + 1
        };
        String[] labels = {
            "present - middle value",
            "absent - larger than every value"
        };

        List<List<Race.Run>> searchRaces = new ArrayList<>();
        for (int i = 0; i < targets.length; i++) {
            searchRaces.add(Race.recordSearches(sorted, targets[i]));
        }

        double[] averages = Race.averageProbes(sorted);

        Visualizer.hideCursor();
        try {
            Visualizer.clearScreen();
            Visualizer.animateSortRace(sortRuns, size, seed, maxValue);
            Visualizer.pause(900);

            Visualizer.clearScreen();
            for (int i = 0; i < targets.length; i++) {
                Visualizer.animateSearchRace(searchRaces.get(i), targets[i],
                        labels[i], size, seed, maxValue);
            }
            Visualizer.pause(900);
        } finally {
            Visualizer.showCursor();
        }

        Visualizer.clearScreen();
        Report.printReport(averages, size, seed, targets, labels);
        return Race.checksPassed;
    }

    static void printIntro() {
        System.out.println(Text.paint(Text.BOLD + Text.CYAN, "Algorithm Race"));
        System.out.println(Text.paint(Text.DIM, "=============="));
        System.out.println(Text.paint(Text.DIM,
                "Three sorting algorithms race on the same data."));
        System.out.println(Text.paint(Text.DIM,
                "Then three search algorithms race on the sorted result."));
        System.out.println();
    }

    static Integer readSize(Scanner scanner) {
        while (true) {
            System.out.print(Text.paint(Text.BOLD,
                    "Array size (4-30) [24]: "));
            System.out.flush();

            if (!scanner.hasNextLine()) {
                System.out.println();
                return null;
            }

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return DEFAULT_SIZE;
            }

            try {
                int value = Integer.parseInt(input);
                if (value >= MIN_SIZE && value <= MAX_SIZE) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Show the message below.
            }

            System.out.println(Text.paint(Text.RED,
                    "Please enter a number from 4 to 30."));
        }
    }

    static Long readSeed(Scanner scanner) {
        while (true) {
            System.out.print(Text.paint(Text.BOLD, "Random seed [42]: "));
            System.out.flush();

            if (!scanner.hasNextLine()) {
                System.out.println();
                return null;
            }

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return DEFAULT_SEED;
            }

            try {
                return Long.parseLong(input);
            } catch (NumberFormatException ignored) {
                System.out.println(Text.paint(Text.RED,
                        "Please enter a whole-number seed."));
            }
        }
    }
}
