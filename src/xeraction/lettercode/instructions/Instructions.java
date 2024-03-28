package xeraction.lettercode.instructions;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class providing easy access to all instructions<br>
 * Using during parsing
 */
public class Instructions {
    /**
     * Every instruction (with an identifier)
     */
    private static final List<Instruction> instructions = new ArrayList<>();

    static {
        instructions.add(new VarInitInstruction());
        instructions.add(new ExitInstruction());
        instructions.add(new PrintInstruction());
        instructions.add(new IfInstruction());
        instructions.add(new WhileInstruction());
    }

    /**
     * Retrieve an instruction from its identifier
     * @param identifier The identifier
     * @return The instruction corresponding to the identifier
     */
    public static Instruction get(char identifier) {
        for (Instruction i : instructions)
            if (i.identifier() == identifier)
                return i;
        return null;
    }
}
