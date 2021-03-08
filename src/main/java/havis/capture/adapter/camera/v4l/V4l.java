package havis.capture.adapter.camera.v4l;

import havis.capture.adapter.camera.Environment;

import java.util.logging.Level;
import java.util.logging.Logger;

public class V4l {

	private final static Logger log = Logger.getLogger(V4l.class.getName());

	static {
		try {
			System.loadLibrary(Environment.V4L);
		} catch (Throwable e) {
			log.log(Level.SEVERE, "Failed to load system library", e);
		}
	}

	public native void open(String s);

	public native void init();

	public native void start();

	public native void stop();

	public native void setResolution(int width, int height);

	public native byte[] capture();

	public native void close();
}
