package xeraction.lettercode.util;

/**
 * Represents a variable
 */
public class Variable {
    /**
     * The name of the variable
     */
    private final String name;

    /**
     * The value of the variable
     */
    private Value value;

    public Variable(String name) {
        this.name = name;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
