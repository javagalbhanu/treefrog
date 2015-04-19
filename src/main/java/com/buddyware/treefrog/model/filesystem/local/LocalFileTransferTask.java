package com.buddyware.treefrog.model.filesystem.local;

import java.util.ArrayList;
import java.util.List;

import com.buddyware.treefrog.BaseTask;
import com.buddyware.treefrog.model.filesystem.SyncPath;

public final class LocalFileTransferTask extends BaseTask {

	private final List<SyncPath> mPaths = new ArrayList<SyncPath>();

	public LocalFileTransferTask() {

	}

	public synchronized final void setPaths(List<SyncPath> paths) {

		if (!mPaths.isEmpty())
			mPaths.clear();

		mPaths.addAll(paths);
	}
}
