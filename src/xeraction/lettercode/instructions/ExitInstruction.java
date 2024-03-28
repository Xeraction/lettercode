package xeraction.lettercode.instructions;

import xeraction.lettercode.Lettercode;
import xeraction.lettercode.util.StringIterator;

/**
 * Represents the "exit" instruction<br>
 * Program exits
 */
public class ExitInstruction implements Instruction {
    public ExitInstruction() {}

    public char identifier() {
        return 'x';
    }

    public Instruction parse(StringIterator iterator) {
        if (iterator.next() != 'l')
            Lettercode.error("Missing end statement after exit instruction", iterator);
        iterator.next();
        return new ExitInstruction();
    }

    public void execute() {
        System.exit(0);
    }
}
