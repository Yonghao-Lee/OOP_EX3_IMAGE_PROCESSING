package ascii_exceptions;

public class CommandFormatException extends  RuntimeException{
    public CommandFormatException(String message){
        super(message);
    }
}
