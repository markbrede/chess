package dataaccess;
 //invalid client input (for HTTP 400)
public class BadRequestException extends DataAccessException {
    public BadRequestException(String message) {
        super(message);
    }
}
