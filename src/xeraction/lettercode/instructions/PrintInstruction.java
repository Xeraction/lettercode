package xeraction.lettercode.instructions;

import xeraction.lettercode.Lettercode;
import xeraction.lettercode.util.StringIterator;
import xeraction.lettercode.util.Value;

/**
 * Represents the "print" instruction<br>
 * Prints a value to the console
 */
public class PrintInstruction implements Instruction {
    /**
     * The value to print
     */
    private Value value;

    public PrintInstruction() {}

    public char identifier() {
        return 'h';
    }

    public Instruction parse(StringIterator iterator) {
        iterator.next();
        //parse the value
        Value value = new Value();
        value.parse(iterator);
        if (iterator.current() != 'l')
            Lettercode.error("Missing end statement after print instruction", iterator);
        iterator.next();
        PrintInstruction inst = new PrintInstruction();
        inst.value = value;
        return inst;
    }

    public void execute() {
        //evaluate a cloned value to support multiple executions in loops with changing variables
        Value v = value.clone();
        v.evaluate();
        System.out.println(v.toStringValue());
    }
}
