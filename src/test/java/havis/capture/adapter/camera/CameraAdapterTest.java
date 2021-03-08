package havis.capture.adapter.camera;

import havis.capture.AdapterException;
import havis.capture.AdapterListener;
import havis.capture.Device;
import havis.capture.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.junit.Assert;
import org.junit.Test;

public class CameraAdapterTest {

	@Test
	public void open(final @Mocked CameraManager manager) throws Exception {
		final List<String> fields = new ArrayList<String>();
		fields.add(FieldConstants.IMAGE);
		fields.add(FieldConstants.RESOLUTION);
		fields.add(FieldConstants.FRAMERATE);
		new NonStrictExpectations() {
			{
				manager.getId();
				result = FieldConstants.DEVICEID;

				manager.getFields();
				result = fields;
			}
		};

		CameraAdapter adapter = null;
		try {
			adapter = new CameraAdapter();
			adapter.open(new AdapterListenerDummy());
		} finally {
			adapter.close();
		}

		Map<String, Device> cams = Deencapsulation.getField(adapter, "cams");
		Device camDevice = cams.get(FieldConstants.DEVICEID);
		Assert.assertNotNull(camDevice);

		Map<String, Field> fieldMap = camDevice.getFields();
		for (String field : fields) {
			Assert.assertTrue(fieldMap.containsKey(field));
		}
		new Verifications() {
			{
				new CameraManager(this.<AdapterListenerDummy> withNotNull());
				times = 1;
			}
		};
	}

	@Test
	public void openWithCameraError(final @Mocked CameraManager manager, final @Mocked CameraManagerException exception) throws Exception {
		final AdapterListener listener = new AdapterListenerDummy();
		new NonStrictExpectations() {
			{
				new CameraManager(listener);
				result = exception;
			}
		};

		CameraAdapter adapter = new CameraAdapter();
		try {
			adapter.open(listener);
		} catch (AdapterException e) {
			Assert.assertEquals(exception, e.getCause());
		}
	}

	@Test
	public void close(final @Mocked CameraManager manager) throws Exception {
		CameraAdapter adapter = new CameraAdapter();
		adapter.open(new AdapterListenerDummy());

		adapter.close();

		new Verifications() {
			{
				manager.close();
				times = 1;
			}
		};
	}

	@Test
	public void closeWithErrorWhileClosing(final @Mocked CameraManagerException exception, final @Mocked CameraManager manager) throws Exception {
		new NonStrictExpectations() {
			{
				manager.close();
				result = exception;
			}
		};

		CameraAdapter adapter = new CameraAdapter();
		adapter.open(new AdapterListenerDummy());

		try {
			adapter.close();
		} catch (AdapterException e) {
			Assert.assertEquals(exception, e.getCause());

			new Verifications() {
				{
					manager.close();
					times = 1;
				}
			};
		}
	}

	@Test
	public void getDevices(final @Mocked CameraManager manager) throws Exception {
		new NonStrictExpectations() {
			{
				manager.getId();
				result = FieldConstants.DEVICEID;
			}
		};

		CameraAdapter adapter = null;
		try {
			adapter = new CameraAdapter();
			adapter.open(new AdapterListenerDummy());

			List<Device> devices = new ArrayList<Device>(adapter.getDevices().values());
			Assert.assertEquals(1, devices.size());
			Assert.assertEquals(FieldConstants.DEVICEID, devices.get(0).getId());
		} finally {
			adapter.close();
		}
	}

	@Test
	public void getProperties() throws Exception {
		CameraAdapter adapter = new CameraAdapter();

		Assert.assertNull(adapter.getProperties());
	}

	@Test
	public void getLabel() throws Exception {
		CameraAdapter adapter = new CameraAdapter();

		Assert.assertNull(adapter.getLabel("device", "label"));
	}

	@Test
	public void add() throws Exception {
		CameraAdapter adapter = new CameraAdapter();

		try {
			adapter.add(new Device());
		} catch (AdapterException e) {
			Assert.assertEquals("Operation not possible", e.getMessage());
		}
	}

	@Test
	public void remove() throws Exception {
		CameraAdapter adapter = new CameraAdapter();

		try {
			adapter.remove("device");
		} catch (AdapterException e) {
			Assert.assertEquals("Operation not possible", e.getMessage());
		}
	}

	@Test
	public void getValue(final @Mocked CameraManager manager) throws Exception {
		new NonStrictExpectations() {
			{
				manager.getValue(FieldConstants.FRAMERATE);
				result = 10;

				manager.getId();
				result = FieldConstants.DEVICEID;

				List<String> fields = new ArrayList<String>();
				fields.add(FieldConstants.FRAMERATE);
				manager.getFields();
				result = fields;

			}
		};
		manager.setValue(FieldConstants.FRAMERATE, 10);
		CameraAdapter adapter = new CameraAdapter();
		adapter.open(new AdapterListenerDummy());

		Assert.assertEquals(10, adapter.getValue(FieldConstants.DEVICEID, FieldConstants.FRAMERATE));
	}

	@Test
	public void getValueWithErrorWhileGettingField(final @Mocked CameraManagerException exception, final @Mocked CameraManager manager) throws Exception {
		new NonStrictExpectations() {
			{
				manager.getValue(this.<String> withNotNull());
				result = exception;
			}
		};

		CameraAdapter adapter = null;
		try {
			adapter = new CameraAdapter();
			adapter.open(new AdapterListenerDummy());

			adapter.getValue(FieldConstants.DEVICEID, FieldConstants.FRAMERATE);
		} catch (AdapterException e) {
			Assert.assertEquals(exception, e.getCause());
		} finally {
			adapter.close();
		}
	}

	@Test
	public void getValueWithUnknownField(final @Mocked CameraManager manager) throws Exception {
		new NonStrictExpectations() {
			{
				List<String> fields = new ArrayList<String>();
				fields.add(FieldConstants.FRAMERATE);
				manager.getFields();
				result = fields;

				manager.getValue(FieldConstants.FRAMERATE);
				result = 10;

				manager.getId();
				result = FieldConstants.DEVICEID;
			}
		};

		CameraAdapter adapter = null;
		try {
			adapter = new CameraAdapter();
			adapter.open(new AdapterListenerDummy());

			Assert.assertNotNull(adapter.getValue(FieldConstants.DEVICEID, FieldConstants.FRAMERATE));
			Assert.assertNull(adapter.getValue(FieldConstants.DEVICEID, FieldConstants.IMAGE));
		} finally {
			adapter.close();
		}
	}

	@Test
	public void getValueWithErrorGettingField(final @Mocked CameraManagerException exception, final @Mocked CameraManager manager) throws Exception {
		new NonStrictExpectations() {
			{
				List<String> fields = new ArrayList<String>();
				fields.add(FieldConstants.FRAMERATE);
				manager.getFields();
				result = fields;

				manager.getValue(FieldConstants.FRAMERATE);
				result = exception;

				manager.getId();
				result = FieldConstants.DEVICEID;
			}
		};

		CameraAdapter adapter = null;
		try {
			adapter = new CameraAdapter();
			adapter.open(new AdapterListenerDummy());
			try {
				adapter.getValue(FieldConstants.DEVICEID, FieldConstants.FRAMERATE);
			} catch (AdapterException e) {
				Assert.assertEquals(exception, e.getCause());
			}
		} finally {
			adapter.close();
		}
	}

	@Test
	public void setValue(final @Mocked CameraManager manager) throws Exception {
		new NonStrictExpectations() {
			{
				manager.getId();
				result = FieldConstants.DEVICEID;

				List<String> fields = new ArrayList<String>();
				fields.add(FieldConstants.FRAMERATE);
				manager.getFields();
				result = fields;
			}
		};
		CameraAdapter adapter = null;
		try {
			adapter = new CameraAdapter();
			adapter.open(new AdapterListenerDummy());

			adapter.setValue(FieldConstants.DEVICEID, FieldConstants.FRAMERATE, 15);

			new Verifications() {
				{
					Object value;
					manager.setValue(this.<String> withNotNull(), value = withCapture());
					Assert.assertEquals(15, value);
				}
			};
		} finally {
			adapter.close();
		}
	}

	@Test
	public void setValueWithErrorSettingField(final @Mocked CameraManagerException exception, final @Mocked CameraManager manager) throws Exception {
		new NonStrictExpectations() {
			{
				manager.getId();
				result = FieldConstants.DEVICEID;

				List<String> fields = new ArrayList<String>();
				fields.add(FieldConstants.FRAMERATE);
				manager.getFields();
				result = fields;

				manager.setValue(FieldConstants.FRAMERATE, 15);
				result = exception;
			}
		};
		CameraAdapter adapter = null;
		try {
			adapter = new CameraAdapter();
			adapter.open(new AdapterListenerDummy());

			adapter.setValue(FieldConstants.DEVICEID, FieldConstants.FRAMERATE, 15);
		} catch (AdapterException e) {
			Assert.assertEquals(exception, e.getCause());
		} finally {
			adapter.close();
		}
	}
}
