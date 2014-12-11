package com.buddyware.treefrog.filesystem.model;

import java.io.File;
import java.nio.file.Path;

public class SyncPath {

		private final File mFile;
		private final String mPath;

		public SyncPath (Path relPath, File fil) {
			mPath = relPath.toString();
			mFile = fil;
		}
		
		public String getPath() { return mPath; }
		public File getFile() { return mFile; }
}
