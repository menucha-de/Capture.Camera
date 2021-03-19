package havis.capture.adapter.camera;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Environment {

	private final static Logger log = Logger.getLogger(Environment.class.getName());
	private final static Properties properties = new Properties();

	static {
		try (InputStream stream = Environment.class.getClassLoader().getResourceAsStream("havis.capture.adapter.camera.properties")) {
			if (stream != null) {
				properties.load(stream);
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to load environment properties", e);
		}
	}

	public static final String V4L = properties.getProperty("havis.capture.adapter.camera.v4l", "v4l");
	public static final String CONFIG_FILE = properties.getProperty("havis.capture.adapter.camera.configFile", "conf/havis/capture/adapter/camera/config.json");

}