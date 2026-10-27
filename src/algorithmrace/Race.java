package algorithmrace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Operation counters, recorded runs, and all sorting/searching algorithms.
 */
final class Race {

    static long comparisons;
    static long swaps;
    static long probes;
    static List<Frame> recording;
    static boolean checksPassed = true;

    private Race() {
    }

    // ------------------------------------------------------------------
    // Recorded frames and runs
    // ------------------------------------------------------------------

    static final class Frame {
        final int[] values;
        final int first;
        final int second;
        final char kind;
        final String message;
        final long comparisons;
        final long swaps;
        final long probes;

        Frame(int[] values, int first, int second, char kind, String message) {
            this.values = values.clone();
            this.first = first;
            this.second = second;
            this.kind = kind;
            this.message = message;
            this.comparisons = Race.comparisons;
            this.swaps = Race.swaps;
            this.probes = Race.probes;
        }
    }

    static final class Run {
        final String name;
        final List<Frame> frames = new ArrayList<>();

        Run(String name) {
            this.name = name;
        }
    }

    // ------------------------------------------------------------------
    // Recording
    // ------------------------------------------------------------------

    static void beginRun(List<Frame> frames, int[] values, String message) {
        comparisons = 0;
        swaps = 0;
        probes = 0;
        recording = frames;
        addFrame(values, -1, -1, 'I', message);
    }

    static void beginCountingOnly() {
        comparisons = 0;
        swaps = 0;
        probes = 0;
        recording = null;
    }

    static void addFrame(int[] values, int first, int second,
                         char kind, String message) {
        if (recording != null) {
            recording.add(new Frame(values, first, second, kind, message));
        }
    }

    static void endRun() {
        recording = null;
    }

    static int[] makeData(int size, long seed) {
        Random random = new Random(seed);
        int[] data = new int[size];

        int value = 0;
        for (int i = 0; i < size; i++) {
            value += 1 + random.nextInt(9);
            data[i] = value;
        }

        for (int i = size - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = data[i];
            data[i] = data[j];
            data[j] = temp;
        }
        return data;
    }

    static List<Run> recordSorts(int[] data, int[] reference) {
        String[] names = {"Bubble Sort", "Insertion Sort", "Selection Sort"};
        List<Run> runs = new ArrayList<>();

        for (int id = 0; id < 3; id++) {
            Run run = new Run(names[id]);
            int[] copy = data.clone();

            beginRun(run.frames, copy, "initial data");
            runSort(id, copy);
            addFrame(copy, -1, -1, 'D', "sorted");
            endRun();

            if (!Arrays.equals(copy, reference)) {
                checksPassed = false;
            }
            runs.add(run);
        }
        return runs;
    }

    static List<Run> recordSearches(int[] sorted, int target) {
        String[] names = {"Linear Search", "Binary Search", "Jump Search"};
        List<Run> runs = new ArrayList<>();

        for (int id = 0; id < 3; id++) {
            Run run = new Run(names[id]);
            int[] copy = sorted.clone();

            beginRun(run.frames, copy, "target " + target);
            int index = runSearch(id, copy, target);

            int highlight = index;
            if (highlight < 0) {
                for (Frame frame : run.frames) {
                    if (frame.kind == 'P') {
                        highlight = frame.first;
                    }
                }
            }

            addFrame(copy, highlight, -1,
                    index >= 0 ? 'F' : 'N',
                    index >= 0 ? "FOUND #" + index : "NOT FOUND");
            endRun();

            boolean ok = index >= 0
                    ? sorted[index] == target
                    : Arrays.binarySearch(sorted, target) < 0;
            if (!ok) {
                checksPassed = false;
            }
            runs.add(run);
        }
        return runs;
    }

    static double[] averageProbes(int[] sorted) {
        double[] averages = new double[3];

        for (int id = 0; id < 3; id++) {
            beginCountingOnly();
            for (int key : sorted) {
                int index = runSearch(id, sorted, key);
                if (index < 0 || sorted[index] != key) {
                    checksPassed = false;
                }
            }
            averages[id] = probes / (double) sorted.length;
        }
        return averages;
    }

    // ------------------------------------------------------------------
    // Instrumented operations
    // ------------------------------------------------------------------

    static boolean lessThan(int[] a, int left, int right) {
        comparisons++;
        addFrame(a, left, right, 'C',
                "cmp " + a[left] + " vs " + a[right]);
        return a[left] < a[right];
    }

    static void swap(int[] a, int i, int j) {
        swaps++;
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
        addFrame(a, i, j, 'M', "swap " + i + " <-> " + j);
    }

    static void probe(int[] a, int index) {
        probes++;
        addFrame(a, index, -1, 'P',
                "probe " + index + " = " + a[index]);
    }

    // ------------------------------------------------------------------
    // Sorting algorithms
    // ------------------------------------------------------------------

    static void runSort(int id, int[] a) {
        switch (id) {
            case 0: bubbleSort(a);    break;
            case 1: insertionSort(a); break;
            case 2: selectionSort(a); break;
        }
    }

    static void bubbleSort(int[] a) {
        for (int pass = 0; pass < a.length - 1; pass++) {
            boolean swapped = false;

            for (int i = 0; i < a.length - 1 - pass; i++) {
                if (lessThan(a, i + 1, i)) {
                    swap(a, i, i + 1);
                    swapped = true;
                }
            }

            if (!swapped) {
                break;
            }
        }
    }

    static void insertionSort(int[] a) {
        for (int i = 1; i < a.length; i++) {
            int j = i;
            while (j > 0 && lessThan(a, j, j - 1)) {
                swap(a, j, j - 1);
                j--;
            }
        }
    }

    static void selectionSort(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            int smallest = i;
            for (int j = i + 1; j < a.length; j++) {
                if (lessThan(a, j, smallest)) {
                    smallest = j;
                }
            }
            if (smallest != i) {
                swap(a, i, smallest);
            }
        }
    }

    // ------------------------------------------------------------------
    // Searching algorithms
    // ------------------------------------------------------------------

    static int runSearch(int id, int[] a, int target) {
        switch (id) {
            case 0: return linearSearch(a, target);
            case 1: return binarySearch(a, target);
            case 2: return jumpSearch(a, target);
            default: return -1;
        }
    }

    static int linearSearch(int[] a, int target) {
        for (int i = 0; i < a.length; i++) {
            probe(a, i);
            if (a[i] == target) {
                return i;
            }
            if (a[i] > target) {
                return -1;
            }
        }
        return -1;
    }

    static int binarySearch(int[] a, int target) {
        int low = 0;
        int high = a.length - 1;

        while (low <= high) {
            int middle = low + (high - low) / 2;
            probe(a, middle);

            if (a[middle] == target) {
                return middle;
            }
            if (a[middle] < target) {
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }
        return -1;
    }

    static int jumpSearch(int[] a, int target) {
        int step = (int) Math.sqrt(a.length);
        if (step < 1) {
            step = 1;
        }

        int start = 0;
        while (start < a.length) {
            int end = Math.min(start + step, a.length) - 1;
            probe(a, end);

            if (a[end] == target) {
                return end;
            }

            if (a[end] > target) {
                for (int i = start; i < end; i++) {
                    probe(a, i);
                    if (a[i] == target) {
                        return i;
                    }
                    if (a[i] > target) {
                        return -1;
                    }
                }
                return -1;
            }

            start += step;
        }
        return -1;
    }

    // ------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------

    static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) {
                return false;
            }
        }
        return true;
    }
}
