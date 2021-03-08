package havis.capture.adapter.camera.ui.resourcebundle;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;

public interface ResourceBundle extends ClientBundle {

	public static final ResourceBundle INSTANCE = GWT
			.create(ResourceBundle.class);
	
	@Source("resources/CssResources.css")
	CssResources css();

}
