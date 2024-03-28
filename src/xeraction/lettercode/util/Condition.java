package xeraction.lettercode.util;

import xeraction.lettercode.Lettercode;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a condition inside if/while statements<br>
 * Has two 'modes': simple and chain<br>
 * -Simple: Comparison between two values<br>
 * -Chain: Number of simple condition connected with logical operators (and, or, etc.)
 */

public class Condition {
    /**
     * Whether this condition is a condition chain or not
     */
    private boolean isChain = false;

    /*
    The values for a simple condition
     */
    private Value first;
    private CondOperator op;
    private Value second;

    /**
     * The condition chain if in chain mode<br>
     * List of simple conditions with their operator
     */
    private List<Couple<ChainOperator, Condition>> chain;

    public Condition() {}

    /**
     * Parse the condition
     * @param iterator The iterator with its position at the first character of the condition
     */
    public void parse(StringIterator iterator) {
        //parse the first condition (don't know if it's a chain yet)
        Condition c = parsePartCondition(iterator);
        while (true) {
            //check for chain operators
            ChainOperator op = switch (iterator.current()) {
                case 'a' -> ChainOperator.AND;
                case 'o' -> ChainOperator.OR;
                case 'x' -> ChainOperator.XOR;
                default -> ChainOperator.NONE;
            };
            //stop if not a chain operator
            if (op == ChainOperator.NONE)
                break;
            //initialize chain behavior
            if (!isChain) {
                isChain = true;
                chain = new ArrayList<>();
                chain.add(new Couple<>(ChainOperator.NONE, c));
            }
            iterator.next();
            //parse following conditions and add them to the chain
            Condition cond = parsePartCondition(iterator);
            chain.add(new Couple<>(op, cond));
        }
        if (isChain)
            return;
        first = c.first;
        op = c.op;
        second = c.second;
    }

    /**
     * Parse a simple condition (part condition in chains)
     * @param iterator The iterator with its position at the first character of the part condition
     * @return A simple condition
     */
    private Condition parsePartCondition(StringIterator iterator) {
        Condition cond = new Condition();
        //parse the first comparison value
        cond.first = new Value();
        cond.first.parse(iterator);
        //parse the conditional operator
        cond.op = switch (iterator.current()) {
            case 'e' -> {
                if (iterator.next() == 't')
                    yield CondOperator.EQUAL;
                noOp(iterator);
                yield CondOperator.NONE;
            }
            case 'a' -> {
                if (iterator.next() == 't')
                    yield CondOperator.NEQUAL;
                noOp(iterator);
                yield CondOperator.NONE;
            }
            case 'g' -> {
                if (iterator.next() == 't')
                    yield CondOperator.GREATER;
                else if (iterator.current() == 'e' && iterator.next() == 't')
                    yield CondOperator.GREQUAL;
                noOp(iterator);
                yield CondOperator.NONE;
            }
            case 'l' -> {
                if (iterator.next() == 't')
                    yield CondOperator.LESS;
                else if (iterator.current() == 'e' && iterator.next() == 't')
                    yield CondOperator.LEQUAL;
                noOp(iterator);
                yield CondOperator.NONE;
            }
            default -> {
                Lettercode.error("Unknown conditional operator", iterator);
                yield CondOperator.NONE;
            }
        };
        iterator.next();
        //parse the second comparison value
        cond.second = new Value();
        cond.second.parse(iterator);
        return cond;
    }

    /**
     * Shortcut for throwing an error when parsing of a conditional operator failed
     * @param it The iterator with its position at the error
     */
    private void noOp(StringIterator it) {
        Lettercode.error("Unknown conditional operator", it);
    }

    /**
     * Evaluates the condition<br>
     * Note that this does not change the actual condition meaning it can be evaluated over and over again
     * @return Whether the condition evaluated to true or false
     */
    public boolean evaluate() {
        if (!isChain) {
            //clone the values to keep the variables - used for multiple evaluations in loops
            Value first = this.first.clone();
            Value second = this.second.clone();
            first.evaluate();
            second.evaluate();
            //make sure the compared types are the same
            //TODO possibility for int/double comparisons
            if (first.getType() != second.getType())
                Lettercode.error("Cannot compare two different variable types! (" + first.getType().name() + ", " + second.getType().name() + ")");
            //do the comparisons
            switch (first.getType()) {
                case INT, DOUBLE, CHAR, BOOLEAN -> {
                    double val1 = getDoubleVal(first);
                    double val2 = getDoubleVal(second);
                    return switch (op) {
                        case EQUAL -> val1 == val2;
                        case NEQUAL -> val1 != val2;
                        case LESS -> val1 < val2;
                        case LEQUAL -> val1 <= val2;
                        case GREATER -> val1 > val2;
                        case GREQUAL -> val1 >= val2;
                        case NONE -> false;
                    };
                }
                case STRING -> {
                    String val1 = first.toStringValue();
                    String val2 = first.toStringValue();
                    //string values only allow checking for equality
                    switch (op) {
                        case EQUAL -> {return val1.equals(val2);}
                        case NEQUAL -> {return !val1.equals(val2);}
                        default -> Lettercode.error("You can only compare two strings for equality!");
                    }
                }
            }
            return false;
        }
        //condition is a chain - evaluate and chain them together with their operator
        boolean top = chain.getFirst().second().evaluate();
        for (int i = 1; i < chain.size(); i++) {
            boolean next = chain.get(i).second().evaluate();
            switch (chain.get(i).first()) {
                case AND -> top = top && next;
                case OR -> top = top || next;
                case XOR -> top = top ^ next;
            }
        }
        return top;
    }

    /**
     * Convert a value to a double for easy comparison
     * @param value The value - has to be of type int, double, boolean, or character
     * @return The value converted to a double
     */
    private double getDoubleVal(Value value) {
        if (value.getType() == Value.Type.INT)
            return value.getAsInt();
        if (value.getType() == Value.Type.DOUBLE)
            return value.getAsDouble();
        if (value.getType() == Value.Type.BOOLEAN)
            return value.getAsBool() ? 1 : 0;
        return (int)value.getAsChar();
    }

    /**
     * The conditional operators
     */
    private enum CondOperator {
        EQUAL, NEQUAL, GREATER, GREQUAL, LESS, LEQUAL, NONE
    }

    /**
     * The chain operators (logical operators)
     */
    private enum ChainOperator {
        AND, OR, XOR, NONE
    }
}
