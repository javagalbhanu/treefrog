package com.buddyware.treefrog.filesystem.model;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class SyncPath {
		private final String mRelativePath;
		private final Path mPath;
		private final SyncType mSyncType;

		public SyncPath (Path path, SyncType syncType) {
			mPath = path;
			mSyncType = syncType;
			mRelativePath = "";
		}
		
		public Path getPath() { return mPath; }
		public SyncType getSyncType() { return mSyncType; }
		
		public File getFile() {
			
			if (Files.isDirectory(mPath))
					return null;
			
			return new File(mPath.toString());
		}
		
}
