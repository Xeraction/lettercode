package xeraction.lettercode.instructions;

import xeraction.lettercode.util.StringIterator;

/**
 * Implemented by every instruction<br>
 * Provides methods used while parsing and executing
 */
public interface Instruction {
    /**
     * @return The identifier of the instruction
     */
    char identifier();

    /**
     * Parses the instruction
     * @param iterator The iterator with its position at the identifier
     * @return A new, parsed instance of the instruction
     */
    Instruction parse(StringIterator iterator);

    /**
     * Execute the instruction
     */
    void execute();
}
