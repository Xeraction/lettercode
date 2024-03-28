package xeraction.lettercode.util;

import xeraction.lettercode.Lettercode;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a value used by variables
 */
public class Value {
    /**
     * The value equivalent of the integer "1"
     */
    public static final Value ONE = new Value();

    static {
        ONE.parts.add(ONE.constructPart(Type.INT, "1"));
    }

    /**
     * The different value parts in order
     */
    private List<ValuePart> parts;

    /**
     * Whether this value has been evaluated
     */
    private boolean evaluated = false;

    public Value() {
        parts = new ArrayList<>();
    }

    /**
     * Parse the value
     * @param iterator The iterator with its position at the first character of the value
     */
    public void parse(StringIterator iterator) {
        //parse the starting value (doesn't have an operator)
        parts.add(parsePart(iterator, Operator.NONE));
        while (true) {
            //check for operators and thus subsequent values to be combined into one
            Operator op = switch (iterator.next()) {
                case 'p' -> Operator.PLUS;
                case 'm' -> Operator.MINUS;
                case 'n' -> Operator.TIMES;
                case 'q' -> Operator.DIVIDE;
                case 'y' -> Operator.MODULO;
                default -> Operator.NONE;
            };
            //unknown operator -> value is done (probably)
            if (op == Operator.NONE)
                break;
            iterator.next();
            parts.add(parsePart(iterator, op));
        }
    }

    /**
     * Parse a value part (without operators)
     * @param iterator The iterator with its position at the first character of the value part
     * @param operator The operator preceding this value part
     * @return The parsed value part
     */
    private ValuePart parsePart(StringIterator iterator, Operator operator) {
        char type = iterator.current();
        //check for variable name
        if (type == Character.toUpperCase(type)) {
            String name = iterator.getVarName(true);
            return new VarPart(operator, name);
        }
        //check for user input
        if (type == 'u')
            return new InputPart(operator);
        String value = iterator.getBetween();
        return switch (type) {
            case 'i' -> new IntPart(operator, StringUtil.getInt(value));
            case 'd' -> new DoublePart(operator, StringUtil.getDouble(value));
            case 's' -> new StringPart(operator, StringUtil.handleStringEscapes(value));
            case 'c' -> new CharPart(operator, StringUtil.handleCharEscapes(value));
            case 'b' -> new BoolPart(operator, StringUtil.getBoolean(value));
            default -> {
                Lettercode.error("Unknown value type '" + type + "'", iterator);
                yield null;
            }
        };
    }

    /**
     * Evaluates this value
     */
    public void evaluate() {
        if (evaluated)
            return;
        Couple<Type, String> tv = new Couple<>(Type.UNKNOWN, "");
        //combine all values into tv in order
        for (ValuePart part : parts) {
            Type t = part.type();
            if (t != tv.first()) {
                //check for cases that need extra processing
                switch (t) {
                    case INPUT -> {
                        Couple<Type, String> in = requestInput();
                        tv = combine(tv, in.second(), in.first(), part.operator());
                    }
                    case VAR -> {
                        String name = ((VarPart)part).name;
                        Variable v = VariableManager.get(name);
                        if (v == null)
                            Lettercode.error("Unknown variable: " + name);
                        if (!v.getValue().hasEvaluated())
                            Lettercode.error("Variable has not been evaluated yet? Probably not your fault...");
                        tv = combine(tv, v.getValue().toStringValue(), v.getValue().getType(), part.operator());
                    }
                    default -> tv = combine(tv, part.toString(), t, part.operator());
                }
            } else {
                tv = combine(tv, part.toString(), t, part.operator());
            }
        }
        //final value is tv -> remove all parts, replace with tv -> evaluation done
        parts.removeIf(p -> true);
        parts.add(constructPart(tv.first(), tv.second()));
        evaluated = true;
    }

    /**
     * Combines two values into one using the specified operator
     * @param in The base value
     * @param value The modifier value used to change the base value
     * @param vType The type of the modifier value
     * @param op The operator for changing the value
     * @return The combined value
     */
    private Couple<Type, String> combine(Couple<Type, String> in, String value, Type vType, Operator op) {
        //type of the base value
        Type cType = in.first();
        //actual value of the base value
        String current = in.second();
        //check all possibilities for combining two value types
        switch (cType) {
            //base value is empty
            case UNKNOWN -> {
                current = value;
                cType = vType;
            }
            case INT -> {
                switch (vType) {
                    case INT -> current = String.valueOf((int)arithmetic(current, value, op));
                    case DOUBLE -> {
                        current = String.valueOf(arithmetic(current, value, op));
                        cType = Type.DOUBLE;
                    }
                    //treat character as its codepoint (int)
                    case CHAR -> current = String.valueOf((int)arithmetic(current, String.valueOf((int)value.charAt(0)), op));
                    case BOOLEAN -> {
                        value = prepBool(value);
                        current = String.valueOf((int)arithmetic(current, value, op));
                    }
                    case STRING -> {
                        if (op != Operator.PLUS)
                            Lettercode.error("Unsupported operation for integer and string: " + op.name());
                        current += value;
                        cType = Type.STRING;
                    }
                }
            }
            case DOUBLE -> {
                switch (vType) {
                    case INT, DOUBLE -> current = String.valueOf(arithmetic(current, value, op));
                    //treat character as its codepoint (int)
                    case CHAR -> current = String.valueOf(arithmetic(current, String.valueOf((int)value.charAt(0)), op));
                    case BOOLEAN -> {
                        value = prepBool(value);
                        current = String.valueOf(arithmetic(current, value, op));
                    }
                    case STRING -> {
                        if (op != Operator.PLUS)
                            Lettercode.error("Unsupported operation for double and string: " + op.name());
                        current += value;
                        cType = Type.STRING;
                    }
                }
            }
            case CHAR -> {
                switch (vType) {
                    case INT -> current = String.valueOf((char)((int)arithmetic(String.valueOf((int)value.charAt(0)), value, op))); //combine integer with codepoint of character -> becomes new character
                    case DOUBLE -> Lettercode.error("Cannot combine a character with a double");
                    case CHAR, STRING -> {
                        if (op != Operator.PLUS)
                            Lettercode.error("Unsupported operation for character and " + (vType == Type.CHAR ? "character: " : "string: ") + op.name());
                        current += value;
                        cType = Type.STRING;
                    }
                    case BOOLEAN -> Lettercode.error("Cannot combine a character with a boolean");
                }
            }
            case BOOLEAN -> {
                switch (vType) {
                    case INT -> {
                        current = String.valueOf((int)arithmetic(prepBool(current), value, op));
                        cType = Type.INT;
                    }
                    case DOUBLE -> {
                        current = String.valueOf(arithmetic(prepBool(current), value, op));
                        cType = Type.DOUBLE;
                    }
                    case CHAR -> Lettercode.error("Cannot combine a boolean with a character");
                    case BOOLEAN -> Lettercode.error("Cannot combine a boolean with another boolean");
                    case STRING -> {
                        if (op != Operator.PLUS)
                            Lettercode.error("Unsupported operation for boolean and string: " + op.name());
                        current += value;
                        cType = Type.STRING;
                    }
                }
            }
            case STRING -> {
                if (op != Operator.PLUS)
                    Lettercode.error("Unsupported operation for string: " + op.name());
                current += value;
            }
        }
        return new Couple<>(cType, current);
    }

    /**
     * Performs arithmetic on two values with a specified operator (double precision)
     * @param first The base value
     * @param second The modifier value
     * @param op The operator
     * @return The arithmetic result as a double
     */
    private double arithmetic(String first, String second, Operator op) {
        double a = Double.parseDouble(first);
        double b = Double.parseDouble(second);
        return switch (op) {
            case PLUS -> a + b;
            case MINUS -> a - b;
            case TIMES -> a * b;
            case DIVIDE -> a / b;
            case MODULO -> a % b;
            default -> a;
        };
    }

    /**
     * Prepares a boolean value (turns string into numeric representation)<br>
     * Does not check whether the input value is actually a boolean value
     * @param in The input string
     * @return The output string
     */
    private String prepBool(String in) {
        if (in.equals("true"))
            return "1";
        if (in.equals("false"))
            return "0";
        return in;
    }

    /**
     * Requests user input and checks its type
     * @return The input value with its corresponding type
     */
    private Couple<Type, String> requestInput() {
        String in = JOptionPane.showInputDialog(null, "The program asked for input.", "Input", JOptionPane.QUESTION_MESSAGE);
        if (in.isEmpty())
            return new Couple<>(Type.UNKNOWN, "");

        //check all possible types in order from most to least specific
        try {
            Integer.parseInt(in);
            return new Couple<>(Type.INT, in);
        } catch (Exception ignored) {}

        try {
            Double.parseDouble(in);
            return new Couple<>(Type.DOUBLE, in);
        } catch (Exception ignored) {}

        if (in.length() == 1)
            return new Couple<>(Type.CHAR, in);

        if (in.equals("true") || in.equals("false"))
            return new Couple<>(Type.BOOLEAN, in);

        return new Couple<>(Type.STRING, in);
    }

    /**
     * Shortcut for the method below with the operator set to none
     * @param type The type of the value part
     * @param value The value of the value part
     * @return The constructed value part
     */
    private ValuePart constructPart(Type type, String value) {
        return constructPart(type, value, Operator.NONE);
    }

    /**
     * Constructs the appropriate value part from the given information
     * @param type The type of the value part
     * @param value The value of the value part
     * @param op The operator of the value part
     * @return The constructed value part
     */
    private ValuePart constructPart(Type type, String value, Operator op) {
        return switch (type) {
            case INT -> new IntPart(op, Integer.parseInt(value));
            case DOUBLE -> new DoublePart(op, Double.parseDouble(value));
            case BOOLEAN -> new BoolPart(op, value.equals("true") || value.equals("1"));
            case CHAR -> new CharPart(op, value.charAt(0));
            case INPUT -> new InputPart(op);
            case VAR -> new VarPart(op, value);
            default -> new StringPart(op, value);
        };
    }

    /**
     * Modifies this value - combines this value with another and a given operator and evaluates it
     * @param op The combination operator
     * @param value The value to be combined with
     */
    public void modify(Operator op, Value value) {
        evaluated = false;
        ValuePart first = value.parts.getFirst();
        first = constructPart(first.type(), first.toString(), op);
        parts.add(first);
        for (int i = 1; i < value.parts.size(); i++)
            parts.add(value.parts.get(i));
        evaluate();
    }

    public boolean hasEvaluated() {
        return evaluated;
    }

    public Type getType() {
        return parts.getFirst().type();
    }

    public int getAsInt() {
        return ((IntPart)parts.getFirst()).value;
    }

    public double getAsDouble() {
        return ((DoublePart)parts.getFirst()).value;
    }

    public char getAsChar() {
        return ((CharPart)parts.getFirst()).value;
    }

    public boolean getAsBool() {
        return ((BoolPart)parts.getFirst()).value;
    }

    /**
     * Returns the string representation of the value
     * @return The string representation if evaluated<br>
     * The value of the first value part if not evaluated
     */
    public String toStringValue() {
        return parts.getFirst().toString();
    }

    /**
     * A value part without operators
     */
    private interface ValuePart {
        Operator operator();
        Type type();
    }

    private record IntPart(Operator operator, int value) implements ValuePart {
        public Type type() {return Type.INT;}
        public String toString() {return String.valueOf(value);}
    }
    private record DoublePart(Operator operator, double value) implements ValuePart {
        public Type type() {return Type.DOUBLE;}
        public String toString() {return String.valueOf(value);}
    }
    private record StringPart(Operator operator, String value) implements ValuePart {
        public Type type() {return Type.STRING;}
        public String toString() {return value;}
    }
    private record CharPart(Operator operator, char value) implements ValuePart {
        public Type type() {return Type.CHAR;}
        public String toString() {return String.valueOf(value);}
    }
    private record BoolPart(Operator operator, boolean value) implements ValuePart {
        public Type type() {return Type.BOOLEAN;}
        public String toString() {return value ? "true" : "false";}
    }
    private record VarPart(Operator operator, String name) implements ValuePart {
        public Type type() {return Type.VAR;}
        public String toString() {return name;}
    }
    private record InputPart(Operator operator) implements ValuePart {
        public Type type() {return Type.INPUT;}
        public String toString() {return "";}
    }

    /**
     * The arithmetic operators
     */
    public enum Operator {
        NONE, PLUS, MINUS, TIMES, DIVIDE, MODULO
    }

    /**
     * The types a value can be
     */
    public enum Type {
        STRING, CHAR, INT, DOUBLE, BOOLEAN, VAR, INPUT, UNKNOWN
    }

    /**
     * Creates a new instance of this value
     * @return A cloned value with the same properties
     */
    public Value clone() {
        Value v = new Value();
        v.evaluated = evaluated;
        v.parts = new ArrayList<>();
        v.parts.addAll(parts);
        return v;
    }
}
