package safrain.remotegsh.shell;

/**
 * Throw if you cant get to the server
 * 
 * @author safrain
 * 
 */
public class ConnectionException extends Exception {
	private static final long serialVersionUID = -8596199224949377302L;

	public ConnectionException() {
		super();
	}

	public ConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectionException(String message) {
		super(message);
	}

	public ConnectionException(Throwable cause) {
		super(cause);
	}

}
