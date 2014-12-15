package com.buddyware.treefrog.filesystem.model;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class SyncPath {
		private final Path mRelativePath;
		private final Path mPath;
		private final SyncType mSyncType;

		public SyncPath (Path rootPath, Path path, SyncType syncType) {
			mPath = path;
			mSyncType = syncType;
			mRelativePath = rootPath.relativize(path);
		}
		
		public Path getPath() { return mPath; }
		public SyncType getSyncType() { return mSyncType; }
		public Path getRelativePath() { return mRelativePath; }
		
		public boolean isFile() { return !Files.isDirectory(mPath); }
		
		public File getFile() {
			
			if (!isFile())
					return null;
			
			return new File(mPath.toString());
		}
		
}
