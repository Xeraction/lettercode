package xeraction.lettercode.instructions;

import xeraction.lettercode.Lettercode;
import xeraction.lettercode.util.Condition;
import xeraction.lettercode.util.StringIterator;
import xeraction.lettercode.util.VariableManager;

import java.util.List;

/**
 * Represents the "while" instruction
 * Execute a block of code while a condition is true
 */
public class WhileInstruction implements Instruction {
    /**
     * The condition to check
     */
    private Condition condition;

    /**
     * List of instructions to execute while the condition is true
     */
    private List<Instruction> instructions;

    public WhileInstruction() {}

    public char identifier() {
        return 'r';
    }

    public Instruction parse(StringIterator iterator) {
        iterator.next();

        //parse the condition
        Condition condition = new Condition();
        condition.parse(iterator);

        //parse the instruction
        if (iterator.current() != 't')
            Lettercode.error("Expected code body open", iterator);
        iterator.next();
        List<Instruction> instructions = Lettercode.parse(iterator, true);

        WhileInstruction inst = new WhileInstruction();
        inst.condition = condition;
        inst.instructions = instructions;
        return inst;
    }

    public void execute() {
        while (condition.evaluate()) {
            VariableManager.pushScope();
            for (Instruction i : instructions)
                i.execute();
            VariableManager.popScope();
        }
    }
}
