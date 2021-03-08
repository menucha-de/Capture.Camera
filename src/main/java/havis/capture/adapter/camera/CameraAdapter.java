package havis.capture.adapter.camera;

import havis.capture.Adapter;
import havis.capture.AdapterException;
import havis.capture.AdapterListener;
import havis.capture.AdapterName;
import havis.capture.AdapterServiceClass;
import havis.capture.Device;
import havis.capture.Field;

import java.util.HashMap;
import java.util.Map;

@AdapterName("camera")
@AdapterServiceClass(CameraService.class)
public class CameraAdapter implements Adapter {

	private CameraManager camManager;

	private Map<String, Device> cams = new HashMap<String, Device>();

	@Override
	public void open(AdapterListener listener) throws AdapterException {
		try {
			camManager = new CameraManager(listener);
		} catch (Exception e) {
			throw new AdapterException(e.getMessage(), e);
		}
		Device camDevice = new Device();
		camDevice.setId(camManager.getId());
		camDevice.setLabel("Logitech Autofocus Cam");

		Map<String, Field> fields = new HashMap<String, Field>();

		for (String fieldId : camManager.getFields()) {
			Field current = new Field();
			current.setId(fieldId);
			current.setLabel(null);
			current.setName(fieldId);
			fields.put(fieldId, current);
		}
		camDevice.setFields(fields);
		cams.put(camManager.getId(), camDevice);
	}

	@Override
	public void close() throws AdapterException {
		try {
			camManager.close();
		} catch (Exception e) {
			throw new AdapterException(e.getMessage(), e);
		}
	}

	@Override
	public Map<String, Device> getDevices() throws AdapterException {
		return cams;
	}

	@Override
	public Object getValue(String deviceId, String fieldId) throws AdapterException {
		Device device = cams.get(deviceId);
		if (device != null && device.getFields().containsKey(fieldId)) {
			try {
				return camManager.getValue(fieldId);
			} catch (Exception e) {
				throw new AdapterException(e.getMessage(), e);

			}
		}
		return null;
	}

	@Override
	public void setValue(String deviceID, String fieldId, Object value) throws AdapterException {
		Device device = cams.get(deviceID);
		if (device != null && device.getFields().containsKey(fieldId)) {
			try {
				camManager.setValue(fieldId, value);
			} catch (Exception e) {
				throw new AdapterException(e.getMessage(), e);
			}
		}
	}

	@Override
	public void setLabel(String device, String label) throws AdapterException {
		throw new AdapterException("Operation not possible");
	}

	@Override
	public void setLabel(String device, String field, String label) throws AdapterException {
		throw new AdapterException("Operation not possible");
	}

	@Override
	public Map<String, String> getProperties() throws AdapterException {
		return null;
	}

	@Override
	public void setProperties(Map<String, String> properties) throws AdapterException {
		throw new AdapterException("Operation not possible");
	}

	@Override
	public void setProperty(String name, String value) throws AdapterException {
		throw new AdapterException("Operation not possible");
	}

	@Override
	public void setProperty(String device, String name, String value) throws AdapterException {
		throw new AdapterException("Operation not possible");
	}

	@Override
	public void setProperty(String device, String field, String name, String value) throws AdapterException {
		throw new AdapterException("Operation not possible");
	}

	@Override
	public void subscribe(String device, String field) throws AdapterException {
	}

	@Override
	public void unsubscribe(String device, String field) throws AdapterException {
	}

	@Override
	public String getLabel(String device) throws AdapterException {
		return null;
	}

	@Override
	public String getLabel(String device, String field) throws AdapterException {
		return null;
	}

	@Override
	public String add(Device device) throws AdapterException {
		throw new AdapterException("Operation not possible");
	}

	@Override
	public void remove(String device) throws AdapterException {
		throw new AdapterException("Operation not possible");
	}

}
