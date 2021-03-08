package havis.capture.adapter.camera.v4l;

public class V4lException extends Exception {

	private static final long serialVersionUID = 1L;

	public V4lException(String message, Throwable cause) {
		super(message, cause);
	}

	public V4lException(Throwable cause) {
		super(cause);
	}

	public V4lException(String message) {
		super(message);
	}

}
