package algorithmrace;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Draws the animated sorting and searching races.
 */
final class Visualizer {

    static final int PANEL_WIDTH = 32;
    static final int PANEL_GAP = 4;
    static final int CONTENT_WIDTH = 104;
    static final int BAR_ROWS = 14;
    static final int MAX_SORT_TICKS = 200;

    static final int SORT_DELAY = 200;
    static final int SEARCH_DELAY = 90;

    // ANSI cursor control only; no colour is used.
    static final String HOME = "\033[H";
    static final String CLEAR_SCREEN = "\033[2J";
    static final String CLEAR_LINE = "\033[K";
    static final String CLEAR_BELOW = "\033[J";
    static final String HIDE_CURSOR = "\033[?25l";
    static final String SHOW_CURSOR = "\033[?25h";

    static {
        Runtime.getRuntime().addShutdownHook(
                new Thread(Visualizer::showCursor));
    }

    private Visualizer() {
    }

    // ------------------------------------------------------------------
    // Animation
    // ------------------------------------------------------------------

    static void animateSortRace(List<Race.Run> runs, int size, long seed,
                                int maxValue) {
        int maxFrames = 0;
        for (Race.Run run : runs) {
            if (run.frames.size() > maxFrames) {
                maxFrames = run.frames.size();
            }
        }

        int step = Math.max(1,
                (int) Math.ceil(maxFrames / (double) MAX_SORT_TICKS));
        int lastFrame = maxFrames - 1;
        int maxTick = (lastFrame + step - 1) / step;

        List<String> screen = new ArrayList<>();

        for (int tick = 0; tick <= maxTick; tick++) {
            int currentFrame = Math.min(tick * step, lastFrame);

            screen.clear();
            screen.add(titleLine("ALGORITHM RACE",
                    "n=" + size + "   seed=" + seed));
            screen.add(progressLine(currentFrame, lastFrame));
            screen.add("");

            List<List<String>> panels = new ArrayList<>();
            for (Race.Run run : runs) {
                int index = Math.min(tick * step, run.frames.size() - 1);
                panels.add(renderPanel(run, index, maxValue, false));
            }
            addPanelRow(screen, panels);

            screen.add("");
            screen.add(sortLegend());
            draw(screen);

            if (tick < maxTick) {
                pause(SORT_DELAY);
            }
        }
        pause(1200);
    }

    static void animateSearchRace(List<Race.Run> runs, int target,
                                  String label, int size, long seed,
                                  int maxValue) {
        int maxFrames = 0;
        for (Race.Run run : runs) {
            if (run.frames.size() > maxFrames) {
                maxFrames = run.frames.size();
            }
        }

        int lastFrame = maxFrames - 1;
        List<String> screen = new ArrayList<>();

        for (int tick = 0; tick <= lastFrame; tick++) {
            screen.clear();
            screen.add(titleLine("SEARCH RACE",
                    "n=" + size + "   seed=" + seed));
            screen.add(targetLine(target, label, tick, lastFrame));
            screen.add("");

            List<List<String>> panels = new ArrayList<>();
            for (Race.Run run : runs) {
                int index = Math.min(tick, run.frames.size() - 1);
                panels.add(renderPanel(run, index, maxValue, true));
            }
            addPanelRow(screen, panels);

            screen.add("");
            screen.add(searchLegend());
            draw(screen);

            if (tick < lastFrame) {
                pause(SEARCH_DELAY);
            }
        }
        pause(1400);
    }

    // ------------------------------------------------------------------
    // Panel drawing
    // ------------------------------------------------------------------

    static List<String> renderPanel(Race.Run run, int frameIndex,
                                    int maxValue, boolean search) {
        int inner = PANEL_WIDTH;
        Race.Frame frame = run.frames.get(
                Math.min(frameIndex, run.frames.size() - 1));
        boolean finished = frameIndex >= run.frames.size() - 1;

        String counts = search
                ? "probes " + frame.probes
                : "cmp " + frame.comparisons + "  swp " + frame.swaps;

        String status;
        if (!search && finished) {
            status = "DONE - sorted";
        } else {
            status = frame.message;
        }

        String titleColor = Text.BOLD + Text.CYAN;
        if (finished) {
            if (search) {
                titleColor = frame.kind == 'F'
                        ? Text.BOLD + Text.GREEN
                        : Text.BOLD + Text.YELLOW;
            } else {
                titleColor = Text.BOLD + Text.GREEN;
            }
        }

        List<String> out = new ArrayList<>();
        out.add(Text.paint(titleColor,
                Text.center(run.name.toUpperCase(Locale.US), inner)));
        out.add(Text.paint(Text.WHITE, Text.center(counts, inner)));

        int n = frame.values.length;
        int leftPad = (inner - n) / 2;

        for (int row = BAR_ROWS; row >= 1; row--) {
            out.add(barRow(frame, row, maxValue, leftPad, inner));
        }

        out.add(markerRow(frame, leftPad, inner));
        out.add(Text.paint(statusColor(frame, finished, search),
                Text.center(status, inner)));
        return out;
    }

    static String barRow(Race.Frame frame, int row, int maxValue,
                         int leftPad, int inner) {
        StringBuilder sb = new StringBuilder();
        sb.append(Text.repeat(' ', leftPad));

        char marker = markerFor(frame.kind);

        for (int i = 0; i < frame.values.length; i++) {
            int height = barHeight(frame.values[i], maxValue);
            if (height >= row) {
                boolean highlighted = marker != ' '
                        && (i == frame.first || i == frame.second);
                if (highlighted) {
                    sb.append(Text.paint(highlightColor(frame.kind), "#"));
                } else {
                    sb.append('#');
                }
            } else {
                sb.append(' ');
            }
        }

        sb.append(Text.repeat(' ', inner - leftPad - frame.values.length));
        return sb.toString();
    }

    static String markerRow(Race.Frame frame, int leftPad, int inner) {
        StringBuilder sb = new StringBuilder();
        sb.append(Text.repeat(' ', leftPad));

        char marker = markerFor(frame.kind);

        for (int i = 0; i < frame.values.length; i++) {
            if (marker != ' ' && (i == frame.first || i == frame.second)) {
                sb.append(Text.paint(highlightColor(frame.kind),
                        String.valueOf(marker)));
            } else {
                sb.append(' ');
            }
        }

        sb.append(Text.repeat(' ', inner - leftPad - frame.values.length));
        return sb.toString();
    }

    static int barHeight(int value, int maxValue) {
        if (maxValue <= 0) {
            return 0;
        }
        int height = (int) Math.ceil(value * (double) BAR_ROWS / maxValue);
        return Math.max(1, height);
    }

    static char markerFor(char kind) {
        switch (kind) {
            case 'C': return '^';
            case 'M': return 'X';
            case 'P': return '?';
            case 'N': return '?';
            case 'F': return '!';
            default:  return ' ';
        }
    }

    static String highlightColor(char kind) {
        switch (kind) {
            case 'C': return Text.YELLOW;
            case 'M': return Text.RED;
            case 'P': return Text.CYAN;
            case 'N': return Text.YELLOW;
            case 'F': return Text.GREEN;
            default:  return Text.WHITE;
        }
    }

    static String statusColor(Race.Frame frame, boolean finished,
                              boolean search) {
        if (finished) {
            if (search) {
                return frame.kind == 'F'
                        ? Text.BOLD + Text.GREEN
                        : Text.BOLD + Text.YELLOW;
            }
            return Text.BOLD + Text.GREEN;
        }

        switch (frame.kind) {
            case 'C': return Text.YELLOW;
            case 'M': return Text.RED;
            case 'P': return Text.CYAN;
            case 'F': return Text.GREEN;
            default:  return Text.DIM;
        }
    }

    static void addPanelRow(List<String> screen,
                            List<List<String>> panels) {
        int height = panels.get(0).size();

        for (int line = 0; line < height; line++) {
            StringBuilder row = new StringBuilder("  ");
            for (int p = 0; p < panels.size(); p++) {
                if (p > 0) {
                    row.append(Text.repeat(' ', PANEL_GAP));
                }
                row.append(panels.get(p).get(line));
            }
            screen.add(row.toString());
        }
    }

    // ------------------------------------------------------------------
    // Header lines
    // ------------------------------------------------------------------

    static String titleLine(String left, String right) {
        int gap = CONTENT_WIDTH - 2 - left.length() - right.length();
        return "  " + Text.paint(Text.BOLD + Text.CYAN, left)
                + Text.repeat(' ', Math.max(1, gap))
                + Text.paint(Text.DIM, right);
    }

    static String progressLine(int current, int total) {
        int width = 30;
        int filled = total <= 0
                ? width
                : (int) Math.round(current * (double) width / total);
        int percent = total <= 0
                ? 100
                : (int) Math.round(current * 100.0 / total);

        String label = String.format(Locale.US,
                "Step %3d / %-3d", current, total);
        String pct = String.format(Locale.US, "%3d%%", percent);

        return "  " + Text.paint(Text.DIM, label) + "  ["
                + Text.paint(Text.CYAN, Text.repeat('#', filled))
                + Text.paint(Text.DIM, Text.repeat('-', width - filled))
                + "] " + Text.paint(Text.WHITE, pct);
    }

    static String targetLine(int target, String label,
                             int tick, int lastFrame) {
        String left = "TARGET " + target + "  -  " + label;
        String right = "Tick " + tick + " / " + lastFrame;
        int gap = CONTENT_WIDTH - 2 - left.length() - right.length();

        return "  " + Text.paint(Text.BOLD + Text.CYAN, left)
                + Text.repeat(' ', Math.max(1, gap))
                + Text.paint(Text.DIM, right);
    }

    static String sortLegend() {
        return "  " + Text.paint(Text.DIM, "Legend: # bar") + "   "
                + Text.paint(Text.YELLOW, "^ compare") + "   "
                + Text.paint(Text.RED, "X swap");
    }

    static String searchLegend() {
        return "  " + Text.paint(Text.DIM, "Legend: # bar") + "   "
                + Text.paint(Text.CYAN, "? probe") + "   "
                + Text.paint(Text.GREEN, "! found");
    }

    // ------------------------------------------------------------------
    // Screen control
    // ------------------------------------------------------------------

    static void hideCursor() {
        System.out.print(HIDE_CURSOR);
        System.out.flush();
    }

    static void showCursor() {
        System.out.print(SHOW_CURSOR + Text.RESET);
        System.out.flush();
    }

    static void clearScreen() {
        System.out.print(CLEAR_SCREEN + HOME);
        System.out.flush();
    }

    static void draw(List<String> lines) {
        StringBuilder screen = new StringBuilder();
        screen.append(HOME);

        for (String line : lines) {
            screen.append(line).append(CLEAR_LINE).append('\n');
        }

        screen.append(CLEAR_BELOW);
        System.out.print(screen);
        System.out.flush();
    }

    static void pause(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
