package xeraction.lettercode.util;

import xeraction.lettercode.Lettercode;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a value used by variables
 */
public abstract class Value {

    /**
     * The value succeeding the current one
     */
    private Value next = null;

    /**
     * The operator of this value
     */
    private Operator operator;

    private Value(Operator operator) {
        this.operator = operator;
    }

    /**
     * Parse the value
     * @param iterator The iterator with its position at the first character of the value
     */
    public static Value parse(StringIterator iterator) {

        //parse the starting value (doesn't have an operator)
        Value root = parsePart(iterator, Operator.NONE);
        Value current = root;

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
            current.next = parsePart(iterator, op);
            current = current.next;
        }

        return root;
    }

    /**
     * Parse a value part (without operators)
     * @param iterator The iterator with its position at the first character of the value part
     * @param operator The operator preceding this value part
     * @return The parsed value part
     */
    private static Value parsePart(StringIterator iterator, Operator operator) {
        char type = iterator.current();
        //check for variable name
        if (type == Character.toUpperCase(type)) {
            String name = iterator.getVarName(true);
            return new VarValue(name, operator);
        }
        //check for user input
        if (type == 'u')
            return new InputValue(operator);
        String value = iterator.getBetween();
        return switch (type) {
            case 'i' -> new IntValue(StringUtil.getInt(value), operator);
            case 'd' -> new DoubleValue(StringUtil.getDouble(value), operator);
            case 's' -> new StringValue(StringUtil.handleStringEscapes(value), operator);
            case 'c' -> new CharValue(StringUtil.handleCharEscapes(value), operator);
            case 'b' -> new BooleanValue(StringUtil.getBoolean(value), operator);
            default -> {
                Lettercode.error("Unknown value type '" + type + "'", iterator);
                yield null;
            }
        };
    }

    /**
     * Evaluates this value
     * @return The evaluated value as a new instance or itself if already evaluated
     */
    public String evaluate() {
        Type type = getType();
        String value = getValue();

        if (value.isBlank() || next == null)
            return value;

        Value nextVal = next;

        while (nextVal != null) {
            Type nextType = next.getType();
            String nextValue = next.getValue();

            if (nextType == Type.VAR || nextType == Type.INPUT)
                nextType = getTypeFromString(nextValue);

            type = mergeTypes(type, nextType);
            value = appendToValue(value, type, nextValue, nextType, nextVal.operator);

            nextVal = nextVal.next;
        }

        return value;
    }

    /**
     * Appends a value to another one
     * @param value The source value to be appended to
     * @param valType The type of the source value
     * @param appendage The value to append
     * @param appendType The type of the append value
     * @param op The operator to use
     * @return The combined value
     */
    private static String appendToValue(String value, Type valType, String appendage, Type appendType, Operator op) {
        //if one of the values is a string, concatenate
        if (valType == Type.STRING || appendType == Type.STRING) {
            if (op != Operator.PLUS)
                throw new RuntimeException("Tried to use an operator other than '+' on strings.");
            return value + appendage;
        }

        //otherwise, do math
        return String.valueOf(arithmetic(value, appendage, op));
    }

    /**
     * Merges two types into the logically correct one
     * @param current The root type
     * @param next The type to be merged into the other
     * @return The merged type
     */
    private Type mergeTypes(Type current, Type next) {
        if (next.canMergeInto(current))
            return current;
        if (current.canMergeInto(next))
            return next;
        throw new RuntimeException("Unable to merge types " + current.name() + " and " + next.name());
    }

    /**
     * Returns the type of this value
     * @return The type of this specific value, not the entire chain
     */
    private Type getType() {
        return switch (this) {
            case StringValue ig -> Type.STRING;
            case CharValue ig -> Type.CHAR;
            case IntValue ig -> Type.INT;
            case DoubleValue ig -> Type.DOUBLE;
            case BooleanValue ig -> Type.BOOLEAN;
            case VarValue ig -> Type.VAR;
            case InputValue ig -> Type.INPUT;
            default -> Type.UNKNOWN;
        };
    }

    /**
     * Gets the type of a string value
     * @param val The value
     * @return The inferred type
     */
    public static Type getTypeFromString(String val) {
        //check all possible types in order from most to least specific
        try {
            Integer.parseInt(val);
            return Type.INT;
        } catch (Exception ignored) {}

        try {
            Double.parseDouble(val);
            return Type.DOUBLE;
        } catch (Exception ignored) {}

        if (val.length() == 1)
            return Type.CHAR;

        if (val.equals("true") || val.equals("false"))
            return Type.BOOLEAN;

        return Type.STRING;
    }

    /**
     * Returns the value of this value
     * @return The value of this specific value, not the entire chain
     */
    private String getValue() {
        return switch (this) {
            case StringValue sv -> sv.value;
            case CharValue cv -> String.valueOf(cv.value);
            case IntValue iv -> String.valueOf(iv.value);
            case DoubleValue dv -> String.valueOf(dv.value);
            case BooleanValue bv -> bv.value ? "true" : "false";
            case VarValue vv -> VariableManager.get(vv.varName).getValue();
            case InputValue iv -> requestInput();
            default -> "";
        };
    }

    /**
     * Performs arithmetic on two values with a specified operator (double precision)
     * @param first The base value
     * @param second The modifier value
     * @param op The operator
     * @return The arithmetic result as a double
     */
    private static double arithmetic(String first, String second, Operator op) {
        double a = Double.parseDouble(prepNumber(first));
        double b = Double.parseDouble(prepNumber(second));
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
    private static String prepNumber(String in) {
        if (in.equals("true"))
            return "1";
        if (in.equals("false"))
            return "0";
        if (in.length() == 1 && !Character.isDigit(in.charAt(0)))
            return String.valueOf((int)in.charAt(0));
        return in;
    }

    /**
     * Requests user input
     * @return The input value
     */
    private String requestInput() {
        return JOptionPane.showInputDialog(null, "The program asked for input.", "Input", JOptionPane.QUESTION_MESSAGE);
    }

    public static String modify(String val, String mod, Operator op) {
        return appendToValue(val, getTypeFromString(val), mod, getTypeFromString(mod), op);
    }

    public static class StringValue extends Value {
        public final String value;

        public StringValue(String value, Operator op) {
            super(op);
            this.value = value;
        }
    }

    public static class CharValue extends Value {
        public final char value;

        public CharValue(char value, Operator op) {
            super(op);
            this.value = value;
        }
    }

    public static class IntValue extends Value {
        public final int value;

        public IntValue(int value, Operator op) {
            super(op);
            this.value = value;
        }
    }

    public static class DoubleValue extends Value {
        public final double value;

        public DoubleValue(double value, Operator op) {
            super(op);
            this.value = value;
        }
    }

    public static class BooleanValue extends Value {
        public final boolean value;

        public BooleanValue(boolean value, Operator op) {
            super(op);
            this.value = value;
        }
    }

    public static class VarValue extends Value {
        public final String varName;

        public VarValue(String varName, Operator op) {
            super(op);
            this.varName = varName;
        }
    }

    public static class InputValue extends Value {
        public InputValue(Operator op) {
            super(op);
        }
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
        STRING,
        DOUBLE(STRING),
        INT(DOUBLE, STRING),
        CHAR(INT, DOUBLE, STRING),
        BOOLEAN(CHAR, INT, DOUBLE, STRING),
        VAR(BOOLEAN, CHAR, INT, DOUBLE, STRING),
        INPUT(BOOLEAN, CHAR, INT, DOUBLE, STRING),
        UNKNOWN;

        private final Type[] canMergeInto;

        Type(Type... canMergeInto) {
            this.canMergeInto = canMergeInto;
        }

        public boolean canMergeInto(Type other) {
            if (canMergeInto == null)
                return false;

            for (Type t : canMergeInto)
                if (t.equals(other))
                    return true;
            return false;
        }
    }
}
