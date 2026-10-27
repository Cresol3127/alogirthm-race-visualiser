package algorithmrace;

/**
 * Small helpers for building fixed-width ASCII output.
 */
final class Text {

    static final String RESET = "\033[0m";
    static final String BOLD = "\033[1m";
    static final String DIM = "\033[2m";
    static final String CYAN = "\033[36m";
    static final String GREEN = "\033[32m";
    static final String YELLOW = "\033[33m";
    static final String RED = "\033[31m";
    static final String WHITE = "\033[97m";

    static final boolean USE_COLOR =
            System.getenv("NO_COLOR") == null;

    private Text() {
    }

    static String paint(String color, String text) {
        if (!USE_COLOR || color == null || color.isEmpty()) {
            return text;
        }
        return color + text + RESET;
    }

    static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    static String center(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int left = (width - text.length()) / 2;
        return repeat(' ', left) + text
                + repeat(' ', width - text.length() - left);
    }
}
