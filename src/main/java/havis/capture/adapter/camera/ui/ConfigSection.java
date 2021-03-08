package havis.capture.adapter.camera.ui;

import havis.capture.adapter.camera.FieldConstants;
import havis.capture.adapter.camera.async.CameraAdapterServiceAsync;
import havis.net.rest.shared.data.SerializableValue;

import java.util.HashMap;
import java.util.Map;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class ConfigSection extends Composite {

	private CameraAdapterServiceAsync service = GWT.create(CameraAdapterServiceAsync.class);

	private static ConfigSectionUiBinder uiBinder = GWT.create(ConfigSectionUiBinder.class);

	interface ConfigSectionUiBinder extends UiBinder<Widget, ConfigSection> {
	}

	@UiField
	TextBox framerate;

	@UiField
	ListBox resolution;

	@UiConstructor
	public ConfigSection() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void refreshConfig() {
		service.getValue(FieldConstants.DEVICEID, FieldConstants.FRAMERATE, new MethodCallback<SerializableValue<String>>() {

			@Override
			public void onSuccess(Method arg0, SerializableValue<String> arg1) {
				framerate.setText(arg1.getValue());
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
			}

		});
		service.getValue(FieldConstants.DEVICEID, FieldConstants.RESOLUTION, new MethodCallback<SerializableValue<String>>() {
			@Override
			public void onSuccess(Method method, SerializableValue<String> response) {
				for (int i = 0; i < resolution.getItemCount(); i++) {
					if (resolution.getValue(i).equals(response.getValue())) {
						resolution.setSelectedIndex(i);
						break;
					}
				}
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
			}
		});
	}

	@UiHandler("framerate")
	void onFramerateChange(ChangeEvent event) {
		Map<String, String> configMap = new HashMap<String, String>();
		configMap.put(FieldConstants.FRAMERATE, framerate.getText());
		service.validateConfiguration(configMap, new MethodCallback<Map<String, String>>() {
			@Override
			public void onSuccess(Method method, Map<String, String> response) {
				String frError = response.get(FieldConstants.FRAMERATE);
				if (frError != null) {
					ErrorViewer.showError(frError);
				}
				if (response.isEmpty()) {
					service.setValue(FieldConstants.DEVICEID, FieldConstants.FRAMERATE, new SerializableValue<String>(framerate.getText()),
							new MethodCallback<Void>() {
								@Override
								public void onSuccess(Method method, Void response) {
								}

								@Override
								public void onFailure(Method method, Throwable exception) {
								}
							});
				}
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
			}
		});
	}

	@UiHandler("resolution")
	void onResolutionChanged(ChangeEvent event) {
		service.setValue(FieldConstants.DEVICEID, FieldConstants.RESOLUTION, new SerializableValue<String>(resolution.getSelectedItemText()),
				new MethodCallback<Void>() {
					@Override
					public void onSuccess(Method method, Void response) {
					}

					@Override
					public void onFailure(Method method, Throwable exception) {
					}
				});
	}

}
