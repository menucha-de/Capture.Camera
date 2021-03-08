package havis.capture.adapter.camera;

import havis.capture.AdapterException;
import havis.capture.AdapterListener;
import havis.capture.FieldUsabilityChangedEvent;
import havis.capture.FieldValueChangedEvent;
import havis.capture.adapter.camera.v4l.V4l;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CameraManager {
	private final static Logger log = Logger.getLogger(CameraManager.class.getName());
	private final static String DEVICE = "/dev/video0";
	private final static Pattern RESOLUTIONPATTERN = Pattern.compile("^(?<width>[0-9]{3,})x(?<height>[0-9]{3,})$");
	private final static TimeUnit TIMEUNIT = TimeUnit.MILLISECONDS;

	private List<String> fields = new CopyOnWriteArrayList<String>();
	private AdapterListener listener;

	private V4l camera;
	private int pictureInterval = 100; // Take picture

	private byte[] stream = null;
	private int width = 160;
	private int height = 120;
	private int framerate = 10;

	ConfigurationManager manager;

	private ScheduledExecutorService worker;

	public CameraManager(AdapterListener listener) throws Exception {
		this.listener = listener;
		fields.add(FieldConstants.IMAGE);
		fields.add(FieldConstants.RESOLUTION);
		fields.add(FieldConstants.FRAMERATE);

		try {
			manager = new ConfigurationManager();
		} catch (ConfigurationManagerException e) {
			throw new AdapterException(e.getMessage(), e);
		}

		String initialResolution = manager.get().getResolution();
		int initialFrameRate = Integer.parseInt(manager.get().getFramerate());
		parseResolution(initialResolution);
		framerate = initialFrameRate;
		if (framerate != 0) {
			pictureInterval = (int) (1.0 / framerate * 1000.0);
		}
		try {
			camera = new V4l();
			camera.open(DEVICE);
			camera.setResolution(width, height);
			camera.init();
			camera.start();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Cannot initialise camera", e);
			throw new AdapterException(e.getMessage(), e);
		}
		try {
			stream = camera.capture();
		} catch (Exception e) {
			log.log(Level.FINE, "Initial capturing failed. Retry once.", e);
			if (camera != null) {
				camera.stop();
				camera.close();
				Thread.sleep(1000);
				camera.open(DEVICE);
				camera.init();
				camera.start();
				stream = camera.capture();
			}
		}
		startExecutor();
	}

	private void startExecutor() {
		worker = Executors.newScheduledThreadPool(1);
		worker.scheduleAtFixedRate(new ImageRunner(), 0, pictureInterval, TIMEUNIT);
	}

	private void stopExecutor() {
		if (worker != null) {
			worker.shutdownNow();
			try {
				if (!worker.awaitTermination(30, TimeUnit.SECONDS)) {
					IllegalStateException illegalStateException = new IllegalStateException("Termination failed");
					log.log(Level.SEVERE, "Failed to stop executore", illegalStateException);
					throw illegalStateException;
				}
			} catch (InterruptedException e) {
				// ignore
			}
			worker = null;
		}
	}

	private void parseResolution(String initialResolution) {
		Matcher matcher = RESOLUTIONPATTERN.matcher(initialResolution);
		if (matcher.matches()) {
			width = Integer.parseInt(matcher.group("width"));
			height = Integer.parseInt(matcher.group("height"));
		}
	}

	public Object getValue(String fieldId) throws Exception {
		switch (fieldId) {
		case FieldConstants.IMAGE:
			return Arrays.copyOf(stream, stream.length);
		case FieldConstants.RESOLUTION:
			return width + "x" + height;
		case FieldConstants.FRAMERATE:
			return Integer.toString(framerate);
		default:
			throw new CameraManagerException("Field not found");
		}
	}

	public void setValue(String fieldId, Object value) throws Exception {
		switch (fieldId) {
		case FieldConstants.RESOLUTION:
			if (camera != null) {
				stopExecutor();
				try {
					camera.stop();
					camera.close();
					parseResolution((String) value);
					camera.setResolution(width, height);
					camera.open(DEVICE);
					camera.init();
					camera.start();
					// Set config
					manager.get().setResolution((String) value);
					manager.set(manager.get());
				} catch (Exception e) {
					log.log(Level.SEVERE, "Reinit failed: ", e);
					throw (e);
				}
				startExecutor();
			}
			break;
		case FieldConstants.FRAMERATE:
			framerate = Integer.parseInt((String) value);
			if (framerate != 0) {
				pictureInterval = (int) (1.0 / framerate * 1000.0);
				stopExecutor();
				startExecutor();
				manager.get().setFramerate((String) value);
				manager.set(manager.get());
			}
			break;
		default:
			throw new CameraManagerException("Property not supported");
		}

	}

	public void close() throws Exception {
		stopExecutor();
		if (camera != null) {
			camera.stop();
			camera.close();
		}
	}

	public String getId() {
		return FieldConstants.DEVICEID;
	}

	private class ImageRunner implements Runnable {
		@Override
		public void run() {
			try {
				stream = camera.capture();
				if (stream != null) {
					byte image[] = Arrays.copyOf(stream, stream.length);
					if (listener != null) {
						FieldValueChangedEvent event = new FieldValueChangedEvent(FieldConstants.DEVICEID, FieldConstants.IMAGE, image);
						listener.valueChanged(null, event);
					}
				}
			} catch (Exception e) {
				log.log(Level.FINE, "Cannot capture image.", e);
				if (listener != null) {
					FieldUsabilityChangedEvent event = new FieldUsabilityChangedEvent(FieldConstants.DEVICEID, FieldConstants.IMAGE, false);
					listener.usabilityChanged(null, event);
				}
			}
		}
	}

	public List<String> getFields() {
		return fields;
	}

}
