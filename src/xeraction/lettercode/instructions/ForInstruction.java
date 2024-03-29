package xeraction.lettercode.instructions;

import xeraction.lettercode.Lettercode;
import xeraction.lettercode.util.Condition;
import xeraction.lettercode.util.StringIterator;
import xeraction.lettercode.util.VariableManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the "for" instruction<br>
 * Executes a block of code while a condition is true, with options for start variables and their modification
 */
public class ForInstruction implements Instruction {
    /**
     * The first part of the loop head<br>
     * Variable initializations or modifications
     */
    private List<Instruction> topStart;

    /**
     * The second part of the loop head<br>
     * The condition that has to be true for the loop to continue
     */
    private Condition topCondition;

    /**
     * The third part of the loop head<br>
     * The variable modifications commonly used for the variables defined in the first part
     */
    private List<Instruction> topLoop;

    /**
     * The actual instructions of the loop body
     */
    private List<Instruction> loop;

    public char identifier() {
        return 'f';
    }

    public Instruction parse(StringIterator iterator) {
        iterator.next();
        ForInstruction inst = new ForInstruction();
        //parse the instructions until a "k" is found
        inst.topStart = new ArrayList<>();
        while (iterator.current() != 'k') {
            Instruction i = Lettercode.parseInstruction(iterator);
            if (!(i instanceof VarInitInstruction) && !(i instanceof VarModifyInstruction))
                Lettercode.error("The first part of a for loop can only contain variable init and modify instructions");
            inst.topStart.add(i);
        }
        iterator.next();
        //parse the condition
        inst.topCondition = new Condition();
        inst.topCondition.parse(iterator);
        if (iterator.current() != 'k')
            Lettercode.error("Unexpected character in for loop", iterator);
        iterator.next();
        //parse the instructions until the code body begins
        inst.topLoop = new ArrayList<>();
        while (iterator.current() != 't') {
            Instruction i = Lettercode.parseInstruction(iterator);
            if (!(i instanceof VarModifyInstruction))
                Lettercode.error("The third part of a for loop can only contain variable modify instructions");
            inst.topLoop.add(i);
        }
        iterator.next();
        //parse the code body
        inst.loop = Lettercode.parse(iterator, true);
        return inst;
    }

    public void execute() {
        //define extra scope for loop head
        VariableManager.pushScope();
        //execute the first part of the loop head
        for (Instruction i : topStart)
            i.execute();

        while (topCondition.evaluate()) {
            //inner loop scope so condition can't use variables defined in the body
            VariableManager.pushScope();
            //execute instructions in loop body
            for (Instruction i : loop)
                i.execute();
            //execute instructions in third part of loop head
            for (Instruction i : topLoop)
                i.execute();
            VariableManager.popScope();
        }
        //make sure variable from loop head can't be used anymore
        VariableManager.popScope();
    }
}
