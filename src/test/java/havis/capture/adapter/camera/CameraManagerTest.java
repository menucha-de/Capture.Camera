package havis.capture.adapter.camera;

import havis.capture.AdapterException;
import havis.capture.adapter.camera.v4l.V4l;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import mockit.VerificationsInOrder;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CameraManagerTest {
	private static final CopyOnWriteArrayList<E> cameraCalls = new CopyOnWriteArrayList<>();
	private static volatile boolean verifyCaptureCall = false;
	private static byte[] captureData = null;
	private static RuntimeException captureException = null;
	private static RuntimeException initException = null;

	public static class E extends AbstractMap.SimpleEntry<String, Object[]> {
		private static final long serialVersionUID = 1L;

		public E(String key, Object[] value) {
			super(key, value);
		}

		public E(String key) {
			super(key, null);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((getKey() == null) ? 0 : getKey().hashCode());
			result = prime * result + Arrays.hashCode(getValue());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (getClass() != obj.getClass())
				return false;
			E other = (E) obj;
			if (getKey() == null) {
				if (other.getKey() != null)
					return false;
			} else if (!getKey().equals(other.getKey()))
				return false;
			if (!Arrays.equals(getValue(), other.getValue()))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getKey() + "(" + (getValue() != null ? Arrays.toString(getValue()) : "") + ")";
		}
	}

	@BeforeClass
	public static void init() {
		new MockUp<V4l>() {
			@Mock
			void $clinit() {
			}

			@Mock
			void open(String device) {
				cameraCalls.add(new E("open", new Object[] { device }));
			}

			@Mock
			void setResolution(int width, int height) {
				cameraCalls.add(new E("setResolution", new Object[] { width, height }));
			}

			@Mock
			void init() {
				cameraCalls.add(new E("init"));
				if (initException != null) {
					throw initException;
				}
			}

			@Mock
			void start() {
				cameraCalls.add(new E("start"));
			}

			@Mock
			void stop() {
				cameraCalls.add(new E("stop"));
			}

			@Mock
			byte[] capture() {
				if (verifyCaptureCall)
					cameraCalls.add(new E("capture"));
				if (captureException != null) {
					throw captureException;
				}
				return captureData;
			}

			@Mock
			void close() {
				cameraCalls.add(new E("close"));
			}
		};
	}

	@Before
	public void before() {
		cameraCalls.clear();
		captureData = null;
		captureException = null;
		initException = null;
	}

	private void assertCallSequence(E... sequence) {
		@SuppressWarnings("unchecked")
		CopyOnWriteArrayList<E> calls = (CopyOnWriteArrayList<E>) cameraCalls.clone();
		List<E> sqList = Arrays.asList(sequence);
		int first = -1;
		while ((first = calls.indexOf(sqList.get(0), first + 1)) > -1) {
			Assert.assertTrue("Expected call sequence " + sqList.toString() + " but was " + calls.toString(), calls.size() >= first + sqList.size());
			Assert.assertEquals(sqList, new ArrayList<E>(calls.subList(first, first + sqList.size())));
			break;
		}
		if (first == -1)
			Assert.fail("Sequence " + sqList.toString() + " could not be found");
	}

	@Test
	public void cameraManager(final @Mocked Executors executors, final @Mocked ScheduledExecutorService worker) throws Exception {
		new NonStrictExpectations() {
			{
				Executors.newScheduledThreadPool(1);
				result = worker;
			}
		};

		AdapterListenerDummy listener = new AdapterListenerDummy();
		List<String> fields = new ArrayList<String>();
		fields.add(FieldConstants.IMAGE);
		fields.add(FieldConstants.RESOLUTION);
		fields.add(FieldConstants.FRAMERATE);

		CameraManager manager = null;

		try {
			manager = new CameraManager(listener);
		} finally {
			worker.shutdown();
		}

		Assert.assertEquals(listener, Deencapsulation.getField(manager, "listener"));
		Assert.assertEquals(fields, Deencapsulation.getField(manager, "fields"));
		Assert.assertEquals(10, (int)Deencapsulation.getField(manager, "framerate"));
		Assert.assertEquals(100, (int)Deencapsulation.getField(manager, "pictureInterval"));

		String DEVICE = "/dev/video0";
		assertCallSequence(new E("open", new Object[] { DEVICE }), new E("setResolution", new Object[] { 320, 240 }), new E("init"), new E("start"));

		new Verifications() {
			{
				worker.scheduleAtFixedRate(this.<Runnable> withNotNull(), 0, 100, TimeUnit.MILLISECONDS);
				times = 1;
			}
		};
	}

	@Test
	public void cameraManagerFailedToInitializeCamera(final @Mocked Logger log) throws Exception {
		initException = new RuntimeException(new Exception());

		try {
			new CameraManager(new AdapterListenerDummy());
		} catch (AdapterException e) {

			String DEVICE = "/dev/video0";
			assertCallSequence(new E("open", new Object[] { DEVICE }), new E("setResolution", new Object[] { 320, 240 }), new E("init"));

			new Verifications() {
				{
					log.log(Level.SEVERE, "Cannot initialise camera", this.<Exception> withNotNull());
					times = 1;
				}
			};
		}
	}

	@Test
	public void cameraManagerConfigurationFailure(final @Mocked ConfigurationManager configManager,
			final @Mocked ConfigurationManagerException configurationException) throws Exception {
		new NonStrictExpectations() {
			{
				new ConfigurationManager();
				result = configurationException;
			}
		};

		try {
			new CameraManager(new AdapterListenerDummy());
		} catch (AdapterException e) {
			Assert.assertEquals(configurationException, e.getCause());
		}
	}

	@Test
	public void cameraManagerRetryCaptureOnStart(final @Mocked Logger log) throws Exception {
		captureException = new RuntimeException(new Exception());
		try {
			new CameraManager(new AdapterListenerDummy());
		} catch (Exception e) {

			String DEVICE = "/dev/video0";
			assertCallSequence(new E("open", new Object[] { DEVICE }), new E("setResolution", new Object[] { 320, 240 }), new E("init"), new E("start"), new E(
					"stop"), new E("close"), new E("open", new Object[] { DEVICE }), new E("init"), new E("start"));
			new Verifications() {
				{
					log.log(Level.FINE, "Initial capturing failed. Retry once.", this.<Exception> withNotNull());
					times = 1;
				}
			};
		}
	}

	@Test
	public void close() throws Exception {
		CameraManager manager = null;
		try {
			manager = new CameraManager(new AdapterListenerDummy());
			ScheduledExecutorService worker = Deencapsulation.getField(manager, "worker");
			Assert.assertNotNull(worker);

			manager.close();

			assertCallSequence(new E("stop"), new E("close"));
			ScheduledExecutorService workerAfter = Deencapsulation.getField(manager, "worker");
			Assert.assertNull(workerAfter);
		} finally {
			manager.close();
		}
	}

	@Test
	public void getId() throws Exception {
		CameraManager manager = null;
		try {
			manager = new CameraManager(new AdapterListenerDummy());

			Assert.assertEquals("cam0", manager.getId());
		} finally {
			manager.close();
		}
	}

	@Test
	public void getFields() throws Exception {
		List<String> expected = new ArrayList<String>();
		expected.add(FieldConstants.IMAGE);
		expected.add(FieldConstants.RESOLUTION);
		expected.add(FieldConstants.FRAMERATE);
		CameraManager manager = null;
		try {
			manager = new CameraManager(new AdapterListenerDummy());

			List<String> actual = manager.getFields();

			Assert.assertEquals(expected, actual);
		} finally {
			manager.close();
		}
	}

	@Test
	public void getValueWithImage(final @Mocked Executors executors) throws Exception {

		CameraManager manager = new CameraManager(new AdapterListenerDummy());
		byte[] stream = new byte[] { 0x00, 0x01 };
		Deencapsulation.setField(manager, "stream", stream);

		byte[] actual = (byte[]) manager.getValue(FieldConstants.IMAGE);

		
		
		Assert.assertArrayEquals(stream, actual);
	}

	@Test
	public void getValueWithResolution() throws Exception {
		CameraManager manager = null;
		try {
			manager = new CameraManager(new AdapterListenerDummy());

			String actual = (String) manager.getValue(FieldConstants.RESOLUTION);

			String defaultResolution = "320x240";
			Assert.assertEquals(defaultResolution, actual);
		} finally {
			manager.close();
		}
	}

	@Test
	public void getValueWithFramerate() throws Exception {
		CameraManager manager = null;
		try {
			manager = new CameraManager(new AdapterListenerDummy());

			String actual = (String) manager.getValue(FieldConstants.FRAMERATE);

			String defaultFramerate = "10";
			Assert.assertEquals(defaultFramerate, actual);
		} finally {
			manager.close();
		}
	}

	@Test
	public void getValueWithInvalidField() throws Exception {
		CameraManager manager = null;
		try {
			manager = new CameraManager(new AdapterListenerDummy());

			manager.getValue("invalidField");
		} catch (CameraManagerException e) {
			Assert.assertEquals("Field not found", e.getMessage());
		} finally {
			manager.close();
		}
	}

	@Test
	public void setValueWithResolution(final @Mocked Executors executors, final @Mocked ScheduledExecutorService worker) throws Exception {
		new NonStrictExpectations() {
			{
				Executors.newScheduledThreadPool(1);
				result = worker;

				worker.awaitTermination(30, TimeUnit.SECONDS);
				result = true;
			}
		};
		CameraManager manager = new CameraManager(new AdapterListenerDummy());

		manager.setValue(FieldConstants.RESOLUTION, "640x480");
		String DEVICE = "/dev/video0";
		assertCallSequence(new E("stop"), new E("close"), new E("setResolution", new Object[] { 640, 480 }), new E("open", new Object[] { DEVICE }), new E(
				"init"), new E("start"));
		Assert.assertEquals("640x480", manager.getValue(FieldConstants.RESOLUTION));
		Assert.assertEquals("640x480", manager.manager.get().getResolution());

		manager.manager.reset();
		new VerificationsInOrder() {
			{
				worker.scheduleAtFixedRate(this.<Runnable> withNotNull(), 0, 100, TimeUnit.MILLISECONDS);
				times = 1;

				worker.shutdownNow();
				times = 1;

				worker.scheduleAtFixedRate(this.<Runnable> withNotNull(), 0, 100, TimeUnit.MILLISECONDS);
				times = 1;
			}
		};
	}

	@Test
	public void setValueWithFramerate(final @Mocked Executors executors, final @Mocked ScheduledExecutorService worker) throws Exception {
		new NonStrictExpectations() {
			{
				Executors.newScheduledThreadPool(1);
				result = worker;

				worker.awaitTermination(30, TimeUnit.SECONDS);
				result = true;
			}
		};
		final String newFramerate = "20";
		CameraManager manager = new CameraManager(new AdapterListenerDummy());

		manager.setValue(FieldConstants.FRAMERATE, newFramerate);
		Assert.assertEquals(newFramerate, manager.getValue(FieldConstants.FRAMERATE));
		Assert.assertEquals(newFramerate, manager.manager.get().getFramerate());

		manager.manager.reset();
		new VerificationsInOrder() {
			{
				worker.scheduleAtFixedRate(this.<Runnable> withNotNull(), 0, 100, TimeUnit.MILLISECONDS);
				times = 1;

				worker.shutdownNow();
				times = 1;

				worker.scheduleAtFixedRate(this.<Runnable> withNotNull(), 0, 50, TimeUnit.MILLISECONDS);
				times = 1;
			}
		};
	}

	@Test
	public void setValueWithInvalidField() throws Exception {
		CameraManager manager = null;
		try {
			manager = new CameraManager(new AdapterListenerDummy());
			manager.setValue("uinvalidField", "2");
		} catch (CameraManagerException e) {
			Assert.assertEquals("Property not supported", e.getMessage());
		} finally {
			manager.close();
		}
	}

	@Test
	public void setValueCannotReinitCamera(final @Mocked Logger log) throws Exception {
		CameraManager manager = null;
		try {
			manager = new CameraManager(new AdapterListenerDummy());
			initException = new RuntimeException(new Exception());
			manager.setValue(FieldConstants.RESOLUTION, "640x480");
		} catch (Exception e) {

			String DEVICE = "/dev/video0";
			assertCallSequence(new E("stop"), new E("close"), new E("setResolution", new Object[] { 640, 480 }), new E("open", new Object[] { DEVICE }), new E(
					"init"));

			new Verifications() {
				{
					log.log(Level.SEVERE, "Reinit failed: ", this.<Exception> withNotNull());
					times = 1;
				}
			};
		}
	}

	@Test
	public void parseResolution() throws Exception {
		String newResolution = "480x360";
		CameraManager manager = null;
		try {
			manager = new CameraManager(new AdapterListenerDummy());
			Assert.assertEquals(320, (int)Deencapsulation.getField(manager, "width"));
			Assert.assertEquals(240, (int)Deencapsulation.getField(manager, "height"));

			Deencapsulation.invoke(manager, "parseResolution", newResolution);

			Assert.assertEquals(480, (int)Deencapsulation.getField(manager, "width"));
			Assert.assertEquals(360, (int)Deencapsulation.getField(manager, "height"));
		} finally {
			manager.manager.reset();
			manager.close();
		}
	}

	@Test
	public void parseResolutionInvalidResolution() throws Exception {
		String newResolution = "invalid";
		CameraManager manager = null;
		try {
			manager = new CameraManager(new AdapterListenerDummy());
			Assert.assertEquals(320, (int)Deencapsulation.getField(manager, "width"));
			Assert.assertEquals(240, (int)Deencapsulation.getField(manager, "height"));

			Deencapsulation.invoke(manager, "parseResolution", newResolution);

			Assert.assertEquals(320, (int)Deencapsulation.getField(manager, "width"));
			Assert.assertEquals(240, (int)Deencapsulation.getField(manager, "height"));
		} finally {
			manager.manager.reset();
			manager.close();
		}
	}

	@Test
	public void startExecutor(final @Mocked Executors executors, final @Mocked ScheduledExecutorService worker) throws Exception {
		new NonStrictExpectations() {
			{
				Executors.newScheduledThreadPool(1);
				result = worker;
			}
		};

		CameraManager manager = new CameraManager(new AdapterListenerDummy());

		Deencapsulation.invoke(manager, "startExecutor");

		new Verifications() {
			{
				Executors.newScheduledThreadPool(1);
				times = 2;

				worker.scheduleAtFixedRate(this.<Runnable> withNotNull(), 0, 100, TimeUnit.MILLISECONDS);
				times = 2;
			}
		};
	}

	@Test
	public void stopExecutor(final @Mocked Executors executors, final @Mocked ScheduledExecutorService worker) throws Exception {
		new NonStrictExpectations() {
			{
				Executors.newScheduledThreadPool(1);
				result = worker;

				worker.awaitTermination(30, TimeUnit.SECONDS);
				result = true;
			}
		};

		CameraManager manager = new CameraManager(new AdapterListenerDummy());

		Deencapsulation.invoke(manager, "stopExecutor");

		Assert.assertNull(Deencapsulation.getField(manager, "worker"));
		new Verifications() {
			{
				worker.shutdownNow();
				times = 1;
			}
		};
	}

	@Test
	public void stopExecutorWithExecutorNotStarted(final @Mocked Executors executors, final @Mocked ScheduledExecutorService worker) throws Exception {
		new NonStrictExpectations() {
			{
				Executors.newScheduledThreadPool(1);
				result = worker;
			}
		};

		CameraManager manager = new CameraManager(new AdapterListenerDummy());
		Deencapsulation.setField(manager, "worker", null);

		Deencapsulation.invoke(manager, "stopExecutor");

		Assert.assertNull(Deencapsulation.getField(manager, "worker"));
		new Verifications() {
			{
				worker.shutdownNow();
				times = 0;
			}
		};
	}

	@Test
	public void stopExecutorFailedToStopExecutor(final @Mocked Logger log, final @Mocked Executors executors, final @Mocked ScheduledExecutorService worker)
			throws Exception {
		new NonStrictExpectations() {
			{
				Executors.newScheduledThreadPool(1);
				result = worker;
			}
		};
		CameraManager manager = new CameraManager(new AdapterListenerDummy());

		try {
			Deencapsulation.invoke(manager, "stopExecutor");
		} catch (IllegalStateException e) {
			Assert.assertEquals("Termination failed", e.getMessage());
		}

		new Verifications() {
			{
				log.log(Level.SEVERE, "Failed to stop executore", this.<IllegalStateException> withNotNull());
				times = 1;
			}
		};
	}

	@Test
	public void imageRunnerCannotCaptureImage(final @Mocked Logger log) throws Exception {
		CameraManager manager = null;
		try {
			manager = new CameraManager(new AdapterListenerDummy());
			captureException = new RuntimeException(new Exception());

			// Invocation + logging occurs only 1 verifies that cancelled = true
			Deencapsulation.invoke(manager, "startExecutor");
		} catch (Exception e) {
			new Verifications() {
				{
					log.log(Level.FINE, "Cannot capture image.", this.<Exception> withNotNull());
					times = 1;
				}
			};
		}
	}
	
	@AfterClass
	public static void clear() {
		Cleanup.deleteFolder(new File("conf"));
	}
}
