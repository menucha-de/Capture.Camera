package havis.capture.adapter.camera;

public class CameraManagerException extends Exception {

	private static final long serialVersionUID = 1L;

	public CameraManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public CameraManagerException(Throwable cause) {
		super(cause);
	}

	public CameraManagerException(String message) {
		super(message);
	}

}
