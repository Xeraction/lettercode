package xeraction.lettercode.instructions;

import xeraction.lettercode.Lettercode;
import xeraction.lettercode.util.Condition;
import xeraction.lettercode.util.StringIterator;
import xeraction.lettercode.util.VariableManager;

import java.util.List;

/**
 * Represents the "if" instruction<br>
 * Executes a code block when a condition is true
 * Executes an optional code block otherwise
 */
public class IfInstruction implements Instruction {
    /**
     * The condition to check
     */
    private Condition condition;

    /**
     * List of instructions to execute when the condition is true
     */
    private List<Instruction> ifInstructions;

    /**
     * Whether the instruction has an else body
     */
    private boolean hasElse = false;

    /**
     * List of instructions to execute when the condition is false (optional)
     */
    private List<Instruction> elseInstructions;

    public IfInstruction() {}

    public char identifier() {
        return 'j';
    }

    public Instruction parse(StringIterator iterator) {
        iterator.next();
        //parse the condition
        Condition condition = new Condition();
        condition.parse(iterator);

        //parse the code body
        if (iterator.current() != 't')
            Lettercode.error("Expected code body open", iterator);
        iterator.next();
        List<Instruction> instructions = Lettercode.parse(iterator, true);

        IfInstruction inst = new IfInstruction();
        inst.condition = condition;
        inst.ifInstructions = instructions;

        //check for optional else block
        if (iterator.current() == 'e') {
            if (iterator.next() != 't')
                Lettercode.error("Expected code body open", iterator);
            iterator.next();

            List<Instruction> elses = Lettercode.parse(iterator, true);
            inst.hasElse = true;
            inst.elseInstructions = elses;
        }

        return inst;
    }

    public void execute() {
        if (condition.evaluate()) {
            VariableManager.pushScope();
            for (Instruction i : ifInstructions)
                i.execute();
            VariableManager.popScope();
        } else if (hasElse) {
            VariableManager.pushScope();
            for (Instruction i : elseInstructions)
                i.execute();
            VariableManager.popScope();
        }
    }
}
