package havis.capture.adapter.camera;

import havis.capture.Adapter;
import havis.capture.AdapterListener;
import havis.capture.DeviceUsabilityChangedEvent;
import havis.capture.FieldUsabilityChangedEvent;
import havis.capture.FieldValueChangedEvent;

public class AdapterListenerDummy implements AdapterListener {

	@Override
	public void usabilityChanged(Adapter arg0, DeviceUsabilityChangedEvent arg1) {
	}

	@Override
	public void usabilityChanged(Adapter arg0, FieldUsabilityChangedEvent arg1) {
	}

	@Override
	public void valueChanged(Adapter arg0, FieldValueChangedEvent arg1) {
	}
}
