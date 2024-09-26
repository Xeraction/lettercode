package xeraction.lettercode.instructions;

import xeraction.lettercode.Lettercode;
import xeraction.lettercode.util.StringIterator;
import xeraction.lettercode.util.VariableManager;

import java.util.List;

public class ScopeInstruction implements Instruction {
    /**
     * The instructions inside the scope
     */
    private List<Instruction> body;

    public char identifier() {
        return 't';
    }

    public Instruction parse(StringIterator iterator) {
        iterator.next();
        ScopeInstruction inst = new ScopeInstruction();
        inst.body = Lettercode.parse(iterator, true);
        return inst;
    }

    public void execute() {
        //define the scope
        VariableManager.pushScope();

        for (Instruction i : body)
            i.execute();

        VariableManager.popScope();
    }
}
