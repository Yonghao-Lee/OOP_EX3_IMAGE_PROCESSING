package ascii_art;

import ascii_exceptions.CommandFormatException;
import ascii_exceptions.InvalidCommandException;
import ascii_exceptions.ResolutionStateException;
import ascii_output.AsciiOutput;
import ascii_output.ConsoleAsciiOutput;
import ascii_output.HtmlAsciiOutput;
import image.Image;
import image_char_matching.SubImgCharMatcher;

import java.io.IOException;

/**
 * Responsible for the interface between the user and the asciiArt API through the command line.
 * Utilizes java's exception system to handle invalid commands and illegal formatting of commands. in case
 * of such an invalid input, prints a relevant message back to the console.
 */
public class Shell{
    public static final String OUT_FILENAME = "out.html";
    public static final String OUT_FONT = "Courier New";
    public static final String EXIT_COMMAND = "exit";
    public static final String CHARS_COMMAND = "chars";
    public static final String REMOVE_COMMAND = "remove";
    public static final String RES_COMMAND = "res";
    public static final String REVERSE_COMMAND = "reverse";
    public static final String OUTPUT_COMMAND = "output";
    public static final String ASCII_ART_COMMAND = "asciiArt";
    private final static char[] DEFAULT_CHARS = {'0', '1','2', '3', '4', '5', '6', '7', '8', '9'};
    private final SubImgCharMatcher matcher;
    private Image image;
    private int resolution;
    private AsciiOutput asciiOutput;

    /**
     * Constructs a new Shell instance
     */
    public Shell(){
        matcher = new SubImgCharMatcher(DEFAULT_CHARS);
        resolution = 2;
        asciiOutput = new ConsoleAsciiOutput();
    }

    /**
     * Runs the command line interface, taking one command after the other. ends on receiving "exit"
     * command. Also returns if the image could not be loaded with an appropriate print.
     * @param imageName filename of the image to convert to ascii art
     */
    public void run(String imageName){
        try {
            image = new Image(imageName);
        }catch (IOException e){
            System.out.println("Error loading image " + imageName);
            return;
        }

        while (true){
            System.out.print(">>> ");
            String commandLine = KeyboardInput.readLine();
            String[] args = commandLine.split(" ");

            if (args[0].equals(EXIT_COMMAND)){
                return;
            }

            try {
                executeCommand(args);
            }catch (InvalidCommandException | ResolutionStateException |
                    CommandFormatException | IllegalStateException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * The main method running the ascii art program.
     * @param args array of argument. should hold one argument that is the path to a .png image
     */
    public static void main(String[] args) {
        if (args.length < 1){
            System.err.println("Usage: java Shell <image name>");
            System.exit(1);
        }
        String imageName = args[0];
        Shell shell = new Shell();
        shell.run(imageName);
    }

    // -- Private Methods --

    /**
     * executes the command given in the args array, print error message if any error occur
     * @param args array of arguments, where the first element is the command keyword itself
     */
    private void executeCommand(String[] args) {

        if (args.length < 1){
            throw new CommandFormatException("Missing command line argument");
        }

        switch (args[0]){
            case CHARS_COMMAND:
                for (char c : matcher.getChars()){
                    System.out.print(c + " ");
                }
                System.out.print("\n");
                break;
            case "add":
                addCommand(args);
                break;

            case REMOVE_COMMAND:
                removeCommand(args);
                break;

            case RES_COMMAND:
                resCommand(args);
                break;

            case REVERSE_COMMAND:
                matcher.setReverse(true);
                break;

            case OUTPUT_COMMAND:
                setAsciiOutput(getArg2(args));
                break;

            case ASCII_ART_COMMAND:
                runAsciiAlgorithm();
                break;

            default:
                throw new InvalidCommandException("Did not execute due to incorrect command.");
        }
    }

    /**
     * Executes the res command, that may scale up or down the resolution by 2 if the second argument is
     * "up" or "down" respectively, and if the new resolution is in valid range. resolution can be as small
     * as 1 char and as big as the image's pixel count.
     * Prints out the resolution, whether it was scaled or not.
     * @param args Arguments of the command, where the first element is the command itself
     */
    private void resCommand(String[] args) {
        if (args.length > 1){
            String arg2;
            arg2 = getArg2(args);
            if (arg2.equals("up")){
                setResolution(resolution * 2);
            }
            else if (arg2.equals("down")){
                setResolution(resolution/2);
            }
            else{
                throw new CommandFormatException(
                        "Did not change resolution due to incorrect format.");
            }
        }

        System.out.println("Resolution set to " + resolution + ".");
    }

    /**
     * Executes the remove command, removing chars from the charset available for the algorithm. input can
     * be a single char, a range of char (e.g. 'a-z'), or the 'all' keyword, that removes all characters
     * from the charset.
     * @param args Arguments of the command, where the first element is the command itself
     */
    private void removeCommand(String[] args) {
        String arg2;
        arg2 = getArg2(args);

        if (arg2.equals("all")){
            removeAll();
            return;
        }

        if (arg2.length() == 1){
            matcher.removeChar(arg2.charAt(0));
            return;
        }

        if (arg2.equals("space")){
            matcher.removeChar(' ');
            return;
        }

        String[] removeRangeStr = arg2.split("-");
        if (removeRangeStr.length != 2){
            throw new CommandFormatException("Did not remove due to incorrect format.");
        }

        if (removeRangeStr[0].length() != 1 || removeRangeStr[1].length() != 1){
            throw new CommandFormatException("Did not remove due to incorrect format.");
        }
        removeCharRange(removeRangeStr[0].charAt(0), removeRangeStr[1].charAt(0));
    }

    /**
     * Executes the add command, adding chars to the charset available for the algorithm. input can
     * be a single char, a range of char (e.g. 'a-z'), or the 'all' keyword, that adds all valid characters
     * to the charset.
     * valid chars are only chars in ascii range of 32-126
     * @param args Arguments of the command, where the first element is the command itself
     */
    private void addCommand(String[] args) {
        String arg2 = getArg2(args);
        if (arg2.equals("all")){
            addAll();
            return;
        }

        if (arg2.length() == 1){
            addChar(arg2.charAt(0));
            return;
        }

        if (arg2.equals("space")){
            addChar(' ');
            return;
        }


        String[] addRangeStr = arg2.split("-");
        if (addRangeStr.length != 2){
            throw new CommandFormatException("Did not add due to incorrect format.");
        }

        if (addRangeStr[0].length() != 1 || addRangeStr[1].length() != 1){
            throw new CommandFormatException("Did not add due to incorrect format.");
        }
        addCharRange(addRangeStr[0].charAt(0), addRangeStr[1].charAt(0));
    }

    /**
     * sets the resolution to the given newResolution, throws exception if the newResolution is out of the
     * valid range.
     * @param newResolution the width and height of the ascii art image in chars
     */
    private void setResolution(int newResolution) {
        int maxCharsInRow = image.getWidth();
        int minCharsInRow = image.getWidth()    / image.getHeight();

        if (newResolution > maxCharsInRow || newResolution < minCharsInRow){
            throw new ResolutionStateException(
                    "Did not change resolution due to exceeding boundaries.");
        }
        resolution = newResolution;
    }

    /**
     * returns the second arg in the array, throws CommandFormatException if the array doesn't have a
     * second element
     * @param args Array of arguments
     * @return the second element of the args array
     */
    private static String getArg2(String[] args) {
        if (args.length < 2) throw new CommandFormatException("Add command must have a second argument");
        return args[1];
    }

    /**
     * Adds all valid chars, from ' ' to '~' to the matcher's charset.
     */
    private void addAll(){
        addCharRange(' ', '~');
    }

    /**
     * Adds all chars between start and end (inclusive) to the matcher's charset. the order of the two
     * arguments does not matter.
     * @param start start of the char range
     * @param end   end of the char range
     */
    private void addCharRange(char start, char end){
        if (start > end){
            addCharRange(end, start);
            return;
        }
        for(int i=start; i<end + 1; i++){
            addChar((char) i);
        }
    }

    /**
     * Adds given char to the matcher's charset. will throw exception if the ascii value of c is outside
     * the legal range, that being [32, 126]
     * @param c character to be added
     */
    private void addChar(char c) {
        if (c<32 || c>126){
            throw new IllegalArgumentException("Illegal character " + c);
        }
        matcher.addChar(c);
    }

    /**
     * Removes all chars from the matcher's charset
     */
    private void removeAll(){
        removeCharRange(' ', '~');
    }

    /**
     * Sets the output strategy as one of two options; Console output that prints the ascii image to the
     * console, and HTML output, that creates a html file that has the ascii image in it.
     * Valid arguments are 'console' and 'html'. throws CommandFormatException otherwise.
     * @param outputType name of the output type to use. should be either 'console' or 'html';
     */
    private void setAsciiOutput(String outputType) {
        if (outputType.equals("console")){
            asciiOutput = new ConsoleAsciiOutput();
        }
        else if (outputType.equals("html")){
            asciiOutput = new HtmlAsciiOutput(OUT_FILENAME, OUT_FONT);
        }
        else{
            throw new CommandFormatException("Did not change output method due to incorrect format.");
        }
    }

    /**
     * Runs the ascii art algorithm with the given parameters input by the user so far. outputs the result
     * using the currently set AsciiOutput instance
     */
    private void runAsciiAlgorithm() {
        if (matcher.getChars().size() < 2){
            throw new IllegalStateException("Did not execute. Charset is too small.");
        }
        AsciiArtAlgorithm algorithm = new AsciiArtAlgorithm(image, resolution, matcher);
        char[][] result = algorithm.run();
        asciiOutput.out(result);
    }

    /**
     * removes all chars in given (inclusive) ascii range from the matcher's charset. order of start and end
     * does not matter.
     * @param start start of the ascii range
     * @param end   end of the ascii range
     */
    private void removeCharRange(char start, char end){
        if (start > end){
            removeCharRange(end, start);
            return;
        }
        for(int i=start; i<end + 1; i++){
            matcher.removeChar((char) i);
        }
    }
}