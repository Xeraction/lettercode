package xeraction.lettercode.instructions;

import xeraction.lettercode.Lettercode;
import xeraction.lettercode.util.StringIterator;
import xeraction.lettercode.util.Value;
import xeraction.lettercode.util.Variable;
import xeraction.lettercode.util.VariableManager;

/**
 * Represents the variable modification instruction<br>
 * Modify an existing variable with a value
 */
public class VarModifyInstruction implements Instruction {
    /**
     * The name of the variable to modify
     */
    private String name;

    /**
     * The operator to use on the modification value
     */
    private Value.Operator op;

    /**
     * The modification value
     */
    private Value value;

    /**
     * Whether this instruction is reassigning this variable to a different value (operator ignored)
     */
    private boolean reassign = false;

    /**
     * Whether this instruction increases the value by one
     */
    private boolean plusplus = false;

    /**
     * Whether this instruction decreases the value by one
     */
    private boolean minusminus = false;

    public VarModifyInstruction() {}

    public char identifier() {
        return ' '; //doesn't use the conventional system because it starts with a variable name which can be any uppercase letter
    }

    public Instruction parse(StringIterator iterator) {
        //parse the variable name
        name = iterator.getVarName();
        //parse the operator
        op = switch (iterator.current()) {
            case 'p' -> Value.Operator.PLUS;
            case 'm' -> Value.Operator.MINUS;
            case 'n' -> Value.Operator.TIMES;
            case 'q' -> Value.Operator.DIVIDE;
            case 'y' -> Value.Operator.MODULO;
            case 'e' -> {
                reassign = true;
                yield Value.Operator.NONE;
            }
            default -> Value.Operator.NONE;
        };

        if (!reassign && op == Value.Operator.NONE)
            Lettercode.error("Unknown variable operator", iterator);
        //check for pp and mm
        if (!reassign) {
            char next = iterator.next();
            if (next == 'p' && op == Value.Operator.PLUS)
                plusplus = true;
            else if (next == 'm' && op == Value.Operator.MINUS)
                minusminus = true;
            else if (next != 'e')
                Lettercode.error("Missing variable modification operator", iterator);
        }
        iterator.next();

        //there's no value with pp or mm
        if (!plusplus && !minusminus) {
            value = new Value();
            value.parse(iterator);
        }

        if (iterator.current() != 'l')
            Lettercode.error("Missing end statement after variable modification", iterator);
        iterator.next();

        return this;
    }

    public void execute() {
        Variable var = VariableManager.get(name);
        if (var == null)
            Lettercode.error("Unknown variable: " + name);

        if (reassign) {
            //evaluate a cloned value to support multiple executions in loops with changing variables
            Value v = value.evaluate();
            var.setValue(v);
        } else if (plusplus) {
            var.getValue().modify(Value.Operator.PLUS, Value.ONE.clone());
        } else if (minusminus) {
            var.getValue().modify(Value.Operator.MINUS, Value.ONE.clone());
        } else {
            //modify doesn't evaluate the input value so it's safe to use here
            var.getValue().modify(op, value);
        }
    }
}
