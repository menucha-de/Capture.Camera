package havis.capture.adapter.camera;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationManagerTest {

	@Test
	public void configurationManagerWithDefaultConfig() throws Exception {
		ConfigurationManager manager = new ConfigurationManager();

		Configuration configuration = manager.get();
		Assert.assertEquals("10", configuration.getFramerate());
		Assert.assertEquals("320x240", configuration.getResolution());
	}

	@Test
	public void configurationManagerWithExistingConfigFile(final @Mocked File configFile, final @Mocked ObjectMapper mapper) throws Exception {
		new NonStrictExpectations() {
			{
				configFile.exists();
				result = true;
			}
		};
		new ConfigurationManager();

		new Verifications() {
			{
				mapper.readValue(new File(Environment.CONFIG_FILE), Configuration.class);
				times = 1;
			}
		};
	}

	@Test
	public void reset() throws Exception {
		ConfigurationManager manager = new ConfigurationManager();

		Configuration configuration = manager.get();
		Assert.assertEquals("10", configuration.getFramerate());
		Assert.assertEquals("320x240", configuration.getResolution());

		manager.reset();

		Configuration configurationAfter = manager.get();
		Assert.assertNull(configurationAfter.getFramerate());
		Assert.assertNull(configurationAfter.getResolution());
	}

	@Test
	public void set() throws Exception {
		ConfigurationManager manager = new ConfigurationManager();

		Configuration configuration = manager.get();
		Assert.assertEquals("10", configuration.getFramerate());
		Assert.assertEquals("320x240", configuration.getResolution());

		Configuration newConfiguration = new Configuration();
		newConfiguration.setFramerate("15");
		newConfiguration.setResolution("640x480");

		manager.set(newConfiguration);

		Configuration configurationAfter = manager.get();
		Assert.assertEquals("15", configurationAfter.getFramerate());
		Assert.assertEquals("640x480", configurationAfter.getResolution());

		manager.reset();
	}

	@Test
	public void setWithError(final @Mocked Files files, final @Mocked IOException ioException) throws Exception {
		new NonStrictExpectations() {
			{
				Files.createDirectories(new File(Environment.CONFIG_FILE).toPath().getParent(), new FileAttribute<?>[] {});
				result = ioException;
			}
		};

		ConfigurationManager manager = new ConfigurationManager();

		Configuration configuration = new Configuration();
		configuration.setFramerate("15");
		configuration.setResolution("640x480");

		try {
			manager.set(configuration);
		} catch (ConfigurationManagerException e) {
			Assert.assertEquals(ioException, e.getCause());
		}
	}

	@Test
	public void configurationManagerWithInvalidConfiguration(final @Mocked File configFile, final @Mocked ObjectMapper mapper) throws Exception {
		final Exception e = new Exception();

		new NonStrictExpectations() {
			{
				configFile.exists();
				result = true;

				mapper.readValue(new File(Environment.CONFIG_FILE), Configuration.class);
				result = e;
			}
		};
		try {
			new ConfigurationManager();
		} catch (ConfigurationManagerException e1) {
			Assert.assertSame(e, e1.getCause());
		}

	}

	@AfterClass
	public static void clear() {
		Cleanup.deleteFolder(new File("conf"));
	}

}
