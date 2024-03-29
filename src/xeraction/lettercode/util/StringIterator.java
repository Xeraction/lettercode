package xeraction.lettercode.util;

import xeraction.lettercode.Lettercode;

/**
 * The iterator used for parsing
 */
public class StringIterator {
    /**
     * The string that is being iterated over
     */
    private final String string;

    /**
     * The current position of the iterator
     */
    private int index = 0;

    public StringIterator(String s) {
        string = s;
        //make sure the string doesn't contain any spaces
        if (string.contains(" "))
            Lettercode.error("The string contains a space! ABOOORRRRTTTTT");
    }

    /**
     * Returns the character at the next position and moving it one space forward
     * @return The character at the next position
     */
    public char next() {
        if (hasNext())
            return string.charAt(++index);
        return current();
    }

    /**
     * Returns the character at the current position without affecting it
     * @return The character at the current position
     */
    public char current() {
        return string.charAt(index);
    }

    /**
     * Returns the character at the previous position without affecting it
     * @return The character at the previous position
     */
    public char previous() {
        if (index == 0)
            return string.charAt(0);
        return string.charAt(index - 1);
    }

    /**
     * Returns the character at the next position without affecting it
     * @return The character at the previous position
     */
    public char peek() {
        if (hasNext())
            return string.charAt(index + 1);
        return current();
    }

    /**
     * Checks whether the iterator has reached the end of the string
     * @return true if the iterator has not reached the end of the string yet
     */
    public boolean hasNext() {
        return index < string.length() - 1;
    }

    /**
     * Returns the current position in the string
     * @return The current position in the string
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the content between the character at the current position and the next occurrence of that character, excluding these characters
     * @return The content between the characters
     */
    public String getBetween() {
        char c = current();
        String s = "";
        while (true) {
            if (peek() == c) {
                //check for escape characters
                if (current() != 'g')
                    break;
                if (current() == 'g' && previous() == 'g')
                    break;
            }
            s += next();
        }
        //move the position to after the last character
        next();
        return s;
    }

    /**
     * Shortcut for the method below with toPrevious set to false
     * @return The variable name
     */
    public String getVarName() {
        return getVarName(false);
    }

    /**
     * Returns the variable name starting from the current position
     * @param toPrevious Whether the position of the iterator should be moved to the last character of the variable name after it's done
     * @return The variable name
     */
    public String getVarName(boolean toPrevious) {
        String s = "";
        //stop when letters are no longer uppercase or numbers
        while (Character.isUpperCase(current()) || Character.isDigit(current())) {
            s += current();
            next();
        }
        if (toPrevious)
            index--;
        return s.toLowerCase();
    }
}
