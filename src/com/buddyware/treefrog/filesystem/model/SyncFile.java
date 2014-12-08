package com.buddyware.treefrog.filesystem.model;

import java.io.File;
import java.nio.file.Path;

public class SyncFile {

		private final File mFile;
		private final Path mPath;
		
		public SyncFile (Path relPath, File fil) {
			mPath = relPath;
			mFile = fil;
		}
		
		public Path getPath() { return mPath; }
		public File getFile() { return mFile; }
}
