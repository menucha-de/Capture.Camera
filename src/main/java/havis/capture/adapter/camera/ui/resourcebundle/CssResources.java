package havis.capture.adapter.camera.ui.resourcebundle;

//import havis.net.ui.shared.client.table.CustomTable;

import com.google.gwt.resources.client.CssResource;

public interface CssResources extends CssResource {
	@ClassName("config-area")
	String configArea();

	String commonLabel();

	String configTextboxShort();

	@ClassName("webui-ListBoxShort")
	String webuiListBoxShort();

	/**
	 * Add this style to input fields which shall be highlighted as invalid
	 * 
	 * @return webui-Input-Error
	 */
	@ClassName("webui-Input-Error")
	String webuiInputError();
}
