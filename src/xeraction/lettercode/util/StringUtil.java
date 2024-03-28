package xeraction.lettercode.util;

import xeraction.lettercode.Lettercode;

/**
 * Various utilities for handling strings
 */
public class StringUtil {

    /**
     * Converts a string into an integer. Throws an error if not possible.
     * @param in The string to be converted
     * @return The converted integer
     */
    public static int getInt(String in) {
        try {
            return Integer.parseInt(in);
        } catch (Exception e) {
            Lettercode.error("Invalid integer value. Expected integer, found: " + in);
            return 0;
        }
    }

    /**
     * Converts a string into a double. Throws an error if not possible.
     * @param in The string to be converted
     * @return The converted double
     */
    public static double getDouble(String in) {
        try {
            return Double.parseDouble(in);
        } catch (Exception e) {
            Lettercode.error("Invalid double value. Expected double, found: " + in);
            return 0;
        }
    }

    /**
     * Converts a string into its boolean value. Throws an error if not possible.
     * @param in The input string
     * @return The converted boolean
     */
    public static boolean getBoolean(String in) {
        in = in.toLowerCase();
        if (in.equals("true") || in.equals("1"))
            return true;
        if (in.equals("false") || in.equals("0"))
            return false;
        Lettercode.error("Invalid boolean value. Expected (true, false, 0, 1), found: " + in);
        return false;
    }

    /**
     * Replaces string escape sequences with their equivalent character
     * @param in The input string
     * @return The escaped string
     */
    public static String handleStringEscapes(String in) {
        return in.replace("gg", "g").replace("gn", "\n").replace("gl", " ").replace("gs", "s");
    }

    /**
     * Replaces character escape sequences with their equivalent character
     * @param in The input character
     * @return The escaped character
     */
    public static char handleCharEscapes(String in) {
        return switch (in) {
            case "gc" -> 'c';
            case "gn" -> '\n';
            case "gl" -> ' ';
            case "gg" -> 'g';
            default -> in.charAt(0);
        };
    }
}
