package havis.capture.adapter.camera.ui;

import havis.net.ui.shared.client.event.MessageEvent.MessageType;
import havis.net.ui.shared.client.widgets.CustomMessageWidget;

import org.fusesource.restygwt.client.FailedResponseException;

import com.google.gwt.http.client.Response;

public class ErrorViewer {
	private static String EXCEPTION = "Exception: ";

	public static void showExceptionResponse(Throwable exception) {
		String result = exception.getMessage();
		if (exception instanceof FailedResponseException) {
			Response response = ((FailedResponseException) exception).getResponse();
			result = response.getText();
		}

		int offset = result.indexOf(EXCEPTION);
		if (offset > 0) {
			result = result.substring(offset + EXCEPTION.length(), result.length());
		}
		showError(result);
	}

	public static void showError(String error) {

		CustomMessageWidget errorPanel = new CustomMessageWidget();
		errorPanel.showMessage(error, MessageType.ERROR);
	}
}
