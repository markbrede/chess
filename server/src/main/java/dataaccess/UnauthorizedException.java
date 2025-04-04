package dataaccess;


//missing or invalid credentials (for HTTP 401s)
public class UnauthorizedException extends DataAccessException {
  public UnauthorizedException(String message) {
    super(message);
  }
}
