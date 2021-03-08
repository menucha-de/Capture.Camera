package havis.capture.adapter.camera;

import java.io.File;

public class Cleanup {
	public static void deleteFolder(File folder) {
		File[] files = folder.listFiles();

		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					deleteFolder(file);
				} else {
					if (!file.delete()) {
						System.out.println(file.getAbsolutePath());
					}
				}
			}
			folder.delete();
		}
	}
}
