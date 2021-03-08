package havis.capture.adapter.camera.async;

import havis.capture.Device;
import havis.net.rest.shared.data.SerializableValue;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("../rest/capture/adapter/camera")
public interface CameraAdapterServiceAsync extends RestService {

	@GET
	@Path("devices")
	void getDevices(MethodCallback<Map<String, Device>> callback);

	@GET
	@Path("devices/{device}/fields/{field}/label")
	void getLabel(@PathParam("device") String device, @PathParam("field") String field, MethodCallback<String> callback);

	@PUT
	@Path("devices/{device}/fields/{field}/label")
	void setLabel(@PathParam("device") String device, @PathParam("field") String field, String label, MethodCallback<Void> callback);

	@GET
	@Path("/devices/{device}/fields/{field}")
	void getValue(@PathParam("device") String device, @PathParam("field") String field, MethodCallback<SerializableValue<String>> callback);

	@PUT
	@Path("devices/{device}/fields/{field}")
	void setValue(@PathParam("device") String device, @PathParam("field") String field, SerializableValue<String> value, MethodCallback<Void> callback);

	@POST
	@Path("validate-config")
	void validateConfiguration(Map<String, String> config, MethodCallback<Map<String, String>> methodCallback);

}