package havis.capture.adapter.camera.ui.resourcebundle;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.Constants;

public interface ConstantsResource extends Constants {

	public static final ConstantsResource INSTANCE = GWT
			.create(ConstantsResource.class);

	String configFramerate();

	String configResolution();
}
