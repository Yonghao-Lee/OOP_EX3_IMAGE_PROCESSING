package ascii_exceptions;

public class InvalidCommandException extends RuntimeException{
    public InvalidCommandException(String message){
        super(message);
    }
}
