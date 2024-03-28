package xeraction.lettercode.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Utility class that manages the variables and scopes
 */
public class VariableManager {
    /**
     * All available variables
     */
    private static final List<Variable> variables = new ArrayList<>();

    /**
     * The scope structure represented by the variable names on a stack
     */
    private static final Deque<List<String>> scopes = new ArrayDeque<>();

    /**
     * Initialize a variable on the top scope
     * @param v The variable to initialize
     */
    public static void add(Variable v) {
        variables.add(v);
        if (scopes.isEmpty())
            scopes.push(new ArrayList<>());
        scopes.getFirst().add(v.getName());
    }

    /**
     * Access a variable
     * @param name The variable name
     * @return The variable corresponding to the name
     */
    public static Variable get(String name) {
        for (Variable v : variables)
            if (v.getName().equals(name))
                return v;
        return null;
    }

    /**
     * Create a new scope (new code body opened)
     */
    public static void pushScope() {
        scopes.push(new ArrayList<>());
    }

    /**
     * Remove the top scope and its variables (code body closed)
     */
    public static void popScope() {
        List<String> popped = scopes.pop();
        for (String name : popped)
            variables.removeIf(v -> v.getName().equals(name));
    }
}
