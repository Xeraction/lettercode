package xeraction.lettercode.instructions;

import xeraction.lettercode.Lettercode;
import xeraction.lettercode.util.StringIterator;
import xeraction.lettercode.util.Value;
import xeraction.lettercode.util.Variable;
import xeraction.lettercode.util.VariableManager;

/**
 * Represents the variable initialization instruction<br>
 * Initializes a variable with the specified name and value on the top scope
 */
public class VarInitInstruction implements Instruction {
    /**
     * The variable name
     */
    private String name;

    /**
     * The variable value
     */
    private Value value;

    public VarInitInstruction() {}

    public char identifier() {
        return 'v';
    }

    public Instruction parse(StringIterator iterator) {
        //parse the name
        if (!Character.isUpperCase(iterator.next()))
            Lettercode.error("Expected variable name", iterator);
        String name = iterator.getVarName();
        if (iterator.current() != 'e')
            Lettercode.error("Expected equal operator", iterator);
        iterator.next();
        //parse the value
        Value value = new Value();
        value.parse(iterator);
        if (iterator.current() != 'l')
            Lettercode.error("Missing end statement after variable initialization instruction", iterator);
        iterator.next();
        VarInitInstruction inst = new VarInitInstruction();
        inst.name = name;
        inst.value = value;
        return inst;
    }

    public void execute() {
        //evaluate a cloned value to support multiple executions in loops with changing variables
        Value val = value.clone();
        val.evaluate();
        Variable v = new Variable(name);
        v.setValue(val);
        VariableManager.add(v);
    }
}
