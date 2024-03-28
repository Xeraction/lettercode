package xeraction.lettercode;

import xeraction.lettercode.instructions.ExitInstruction;
import xeraction.lettercode.instructions.Instruction;
import xeraction.lettercode.instructions.Instructions;
import xeraction.lettercode.instructions.VarModifyInstruction;
import xeraction.lettercode.util.StringIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/*
Welcome to Lettercode!
Please follow these insightful and professionally written comments masterfully documenting the inner workings of
this codebase for a better understanding of the structure of the subjectively best esolang out there.
Jokes aside, I tried to make these comments as clear as possible.
Have fun!
 */

public class Lettercode {

    public static void main(String[] args) {
        //make sure the user actually provided a correct .lc file
        if (args.length != 1)
            error("You have to provide a file!");

        File file = new File(args[0]);
        if (!file.exists())
            error("The provided file doesn't exist!");

        //check for .lc extension
        String[] nm = file.getName().split("\\.");
        if (!nm[nm.length - 1].equals("lc"))
            error("The provided file is not a .lc file!");

        //read the file
        List<String> lines = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            lines = reader.lines().toList();
            reader.close();
        } catch (Exception e) {
            error("Couldn't read provided file.", e);
        }
        if (lines.isEmpty())
            error("The provided file is empty!");

        //parse the main code
        String main = lines.getFirst();
        List<Instruction> instructions = parse(new StringIterator(main), false);
        if (instructions.isEmpty())
            Lettercode.error("Empty instruction set. Aborting...");

        //make sure the code ends with an end statement
        if (!(instructions.getLast() instanceof ExitInstruction))
            Lettercode.error("The program doesn't end with an end instruction! Running complicated algorithm trying to guess where to put it... Putting it right here. Goodbye!");

        //execute the instructions
        for (Instruction inst : instructions)
            inst.execute();
    }

    /**
     * Parse a sequence of instructions
     * @param iterator The iterator with its position at the first character of the first instruction to parse
     * @param body Whether parsing should stop when a 'z' (close code body) is reached
     * @return A list of fully parsed instructions
     */
    public static List<Instruction> parse(StringIterator iterator, boolean body) {
        List<Instruction> instructions = new ArrayList<>();

        while (iterator.hasNext()) {
            //check if the end of a code body is reached
            if (body && iterator.current() == 'z') {
                iterator.next();
                return instructions;
            }

            //check if it is a variable modification (uppercase variable name)
            if (Character.isUpperCase(iterator.current())) {
                Instruction inst = new VarModifyInstruction();
                instructions.add(inst.parse(iterator));
                continue;
            }

            //parse the current instruction
            Instruction inst = Instructions.get(iterator.current());
            if (inst == null)
                error("Unknown instruction", iterator);
            instructions.add(inst.parse(iterator));
        }

        //got to the end of the code before the current body was closed
        if (body)
            Lettercode.error("Unclosed code body", iterator);

        return instructions;
    }

    /**
     * Prints an error message and exits
     * @param msg The error message
     * @param iterator The iterator with its position at the error
     */
    public static void error(String msg, StringIterator iterator) {
        System.err.println(msg + " (" + iterator.current() + ", " + iterator.getIndex() + ")");
        System.exit(-1);
    }

    /**
     * Prints an error message and exits
     * @param msg The error message
     */
    public static void error(String msg) {
        System.err.println(msg);
        System.exit(-1);
    }

    /**
     * Prints an error message and exits
     * @param msg The error message
     * @param error An exception with stacktrace to print
     */
    public static void error(String msg, Throwable error) {
        System.err.println(msg);
        error.printStackTrace();
        System.exit(-1);
    }
}
