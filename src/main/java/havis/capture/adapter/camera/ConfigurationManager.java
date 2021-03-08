package havis.capture.adapter.camera;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationManager {

	private final static ObjectMapper mapper = new ObjectMapper();
	private final static String DEFAULT_CONFIG = "havis/capture/adapter/camera/config/default.json";

	private Configuration configuration;

	public ConfigurationManager() throws ConfigurationManagerException {
		super();
		try {
			File configFile = new File(Environment.CONFIG_FILE);
			if (configFile.exists()) {
				this.configuration = mapper.readValue(new File(Environment.CONFIG_FILE), Configuration.class);
			} else {
				this.configuration = mapper.readValue(ConfigurationManager.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG), Configuration.class);
			}
		} catch (Exception e) {
			throw new ConfigurationManagerException(e);
		}
	}

	public Configuration get() {
		return this.configuration;
	}

	public void set(Configuration configuration) throws ConfigurationManagerException {
		try {
			Files.createDirectories(new File(Environment.CONFIG_FILE).toPath().getParent(), new FileAttribute<?>[] {});
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(Environment.CONFIG_FILE), configuration);
			this.diff(this.configuration, configuration);
			this.configuration = configuration;

		} catch (Exception e) {
			throw new ConfigurationManagerException(e);
		}
	}

	private void diff(Configuration oldConf, Configuration newConf) {		
	}

	public void reset() {
		new File(Environment.CONFIG_FILE).delete();
		this.configuration = new Configuration();
	}

}