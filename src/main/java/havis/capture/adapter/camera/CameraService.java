package havis.capture.adapter.camera;

import havis.capture.Adapter;
import havis.capture.AdapterException;
import havis.capture.AdapterHandler;
import havis.capture.AdapterListener;
import havis.capture.AdapterManager;
import havis.capture.Device;
import havis.capture.DeviceUsabilityChangedEvent;
import havis.capture.FieldUsabilityChangedEvent;
import havis.capture.FieldValueChangedEvent;
import havis.capture.rest.AdapterService;
import havis.net.rest.shared.data.SerializableValue;
import havis.net.server.http.provider.PartType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("capture/adapter/camera")
public class CameraService implements AdapterService {

	private final static Logger log = Logger.getLogger(CameraService.class.getName());

	private AdapterManager manager;

	public CameraService(AdapterManager manager) {
		this.manager = manager;
	}

	@PermitAll
	@GET
	@Path("devices/{device}/stream")
	@Produces("multipart/x-mixed-replace")
	@PartType("image/jpeg")
	public Iterator<Object> getStream(@PathParam("device") final String device) throws Exception {
		final BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
		final AdapterHandler handler = manager.createInstance();
		handler.subscribe(device, FieldConstants.IMAGE);
		handler.setListener(new AdapterListener() {
			@Override
			public void valueChanged(Adapter source, FieldValueChangedEvent event) {
				queue.add(event.getValue());
			}

			@Override
			public void usabilityChanged(Adapter source, FieldUsabilityChangedEvent event) {
				if (!event.isUsable()) {
					queue.add(null);
				}
			}

			@Override
			public void usabilityChanged(Adapter source, DeviceUsabilityChangedEvent event) {
			}
		});
		return new Iterator<Object>() {
			boolean hasNext = true;

			@Override
			public boolean hasNext() {
				return hasNext;
			}

			@Override
			public Object next() {
				try {
					Object element = queue.take();
					if (element == null) {
						hasNext = false;
					}
					return element;
				} catch (InterruptedException e) {
					return null;
				}
			}

			@Override
			public void remove() {
				try {
					log.log(Level.FINE, "Stream has been closed.");
					handler.unsubscribe(device, FieldConstants.IMAGE);
					handler.close();
				} catch (Exception e) {
				}
			}
		};
	}

	@PermitAll
	@GET
	@Path("devices/{device}/image")
	@Produces({ "image/jpeg" })
	public Response getImage(@PathParam("device") String device) throws Exception {
		try (AdapterHandler handler = manager.createInstance()) {
			return Response.ok(handler.getValue(device, FieldConstants.IMAGE)).build();
		}
	}

	@PermitAll
	@GET
	@Path("devices")
	@Produces({ MediaType.APPLICATION_JSON })
	@Override
	public Map<String, Device> getDevices() throws AdapterException {
		try (AdapterHandler handler = manager.createInstance()) {
			return handler.getDevices();
		} catch (AdapterException e) {
			throw e;
		} catch (Exception e) {
			log.log(Level.FINE, "Failed to close adapter", e);
		}
		return null;
	}

	@PermitAll
	@GET
	@Path("devices/{device}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Device getDevice(@PathParam("device") String device) throws AdapterException { return null; }

	@PermitAll
	@POST
	@Path("devices")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_PLAIN })
	public String add(Device device) throws AdapterException { return null; }

	@PermitAll
	@DELETE
	@Path("devices/{device}")
	public void remove(@PathParam("device") String device) throws AdapterException {}

	@PermitAll
	@GET
	@Path("devices/{device}/label")
	@Produces({ MediaType.TEXT_PLAIN })
	public String getLabel(@PathParam("device") String device) throws AdapterException { return null; }

	@PermitAll
	@PUT
	@Path("devices/{device}/label")
	@Consumes({ MediaType.TEXT_PLAIN })
	public void setLabel(@PathParam("device") String device, String label) throws AdapterException {}

	@PermitAll
	@GET
	@Path("devices/{device}/fields/{field}/label")
	public String getLabel(String device, String field) {
		try (AdapterHandler handler = manager.createInstance()) {
			return handler.getLabel(device, field);
		} catch (Exception e) {
		}
		return null;
	}

	@PermitAll
	@PUT
	@Path("devices/{device}/fields/{field}/label")
	public void setLabel(String device, String field, String label) throws AdapterException {
		try (AdapterHandler handler = manager.createInstance()) {
			handler.setLabel(device, field, label);
		} catch (Exception e) {
		}
	}

	@PermitAll
	@GET
	@Path("devices/{device}/fields/{field}")
	@Produces({ MediaType.TEXT_PLAIN })
	public String getValue(@PathParam("device") String device, @PathParam("field") String field) throws AdapterException {
		try (AdapterHandler handler = manager.createInstance()) {
			return (String) handler.getValue(device, field);
		} catch (AdapterException e) {
			throw e;
		} catch (Exception e) {
			log.log(Level.FINE, "Failed to close adapter", e);
		}
		return null;
	}

	@PermitAll
	@PUT
	@Path("devices/{device}/fields/{field}")
	@Consumes({ MediaType.TEXT_PLAIN })
	public void setValue(@PathParam("device") String device, @PathParam("field") String field, String value) throws AdapterException {
		try (AdapterHandler handler = manager.createInstance()) {
			handler.setValue(device, field, value);
		} catch (AdapterException e) {
			throw e;
		} catch (Exception e) {
			log.log(Level.FINE, "Failed to close adapter", e);
		}
	}

	@PermitAll
	@GET
	@Path("devices/{device}/properties")
	@Produces({ MediaType.APPLICATION_JSON })
	public Map<String, String> getProperties(@PathParam("device") String device) throws AdapterException { return null; }

	@PermitAll
	@GET
	@Path("devices/{device}/properties/{name}")
	@Produces({ MediaType.TEXT_PLAIN })
	public String getProperty(@PathParam("device") String device, @PathParam("name") String name) throws AdapterException { return null; }

	@PermitAll
	@PUT
	@Path("devices/{device}/properties/{name}")
	@Consumes({ MediaType.TEXT_PLAIN })
	public void setProperty(@PathParam("device") String device, @PathParam("name") String name, String value) throws AdapterException {}
	
	@PermitAll
	@POST
	@Path("subscriptions")
	@Consumes({ MediaType.TEXT_PLAIN })
	@Produces({ MediaType.TEXT_PLAIN })
	public String addSubscription(String uri) throws AdapterException { return null; }

	@PermitAll
	@DELETE
	@Path("subscriptions/{id}")
	public void removeSubscription(@PathParam("id") String id) throws AdapterException {}

	@PermitAll
	@POST
	@Path("subscriptions/{id}/devices/{device}/fields/{field}")
	public void subscribe(@PathParam("id") String id, @PathParam("device") String device, @PathParam("field") String field) throws AdapterException {}

	@PermitAll
	@DELETE
	@Path("subscriptions/{id}/devices/{device}/fields/{field}")
	public void unsubscribe(@PathParam("id") String id, @PathParam("device") String device, @PathParam("field") String field) throws AdapterException {}

	@PermitAll
	@POST
	@Path("validate-config")
	@Produces({ MediaType.APPLICATION_JSON })
	public Map<String, String> validateConfiguration(Map<String, String> config) {
		return validateConfig(config.get(FieldConstants.FRAMERATE));
	}

	private boolean isNullOrEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}

	private Map<String, String> validateConfig(String framerate) {
		Map<String, String> result = new HashMap<>();
		int framerateVal = 0;
		if (framerate != null) {
			if (isNullOrEmpty(framerate)) {
				result.put(FieldConstants.FRAMERATE, "Please specify a Frame rate.");
			} else {
				try {
					framerateVal = Integer.parseInt(framerate);
				} catch (NumberFormatException e) {
					result.put(FieldConstants.FRAMERATE, "Frame rate must be integer value and 0>value<=30.");
				}
			}
		}
		if (framerateVal < 1 || framerateVal > 30) {
			result.put(FieldConstants.FRAMERATE, "Frame rate must be integer value and 0>value<=30.");
		}
		return result;
	}

	@Override
	public void close() throws Exception {
	}

}
