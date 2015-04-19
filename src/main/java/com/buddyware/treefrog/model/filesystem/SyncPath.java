package com.buddyware.treefrog.model.filesystem;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SyncPath {
	private final Path mRelativePath;
	private final Path mPath;
	private final SyncType mSyncType;
	private long mQueuedTime;
	private final int mHash;

	public SyncPath(Path rootPath, Path path, SyncType syncType) {
		mPath = path;
		mSyncType = syncType;
		mRelativePath = rootPath.relativize(path);
		mHash = mPath.hashCode();
	}

	public SyncPath(SyncPath path) {
		mPath = path.getPath();
		mSyncType = path.getSyncType();
		mRelativePath = path.getRelativePath();
		mQueuedTime = path.getQueuedTime();
		mHash = path.hashCode();
	}

	public final synchronized void setQueuedTime(long queuedTime) {
		mQueuedTime = queuedTime;
	}

	public final synchronized Path getPath() {
		return mPath;
	}

	public final synchronized SyncType getSyncType() {
		return mSyncType;
	}

	public final synchronized Path getRelativePath() {
		return mRelativePath;
	}

	public final synchronized long getQueuedTime() {
		return mQueuedTime;
	}

	public final synchronized boolean isFile() {
		return !Files.isDirectory(mPath);
	}

	public final synchronized int hashCode() {
		return mHash;
	}

	public final synchronized File getFile() {

		if (!isFile())
			return null;

		return new File(mPath.toString());
	}

}
