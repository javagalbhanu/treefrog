package com.buddyware.treefrog.filesystem.model.local;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;

import com.buddyware.treefrog.filesystem.model.FileSystem;
import com.buddyware.treefrog.filesystem.model.FileSystemType;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.stage.FileChooser;


public class LocalFileSystem extends FileSystem {
	
	//watchService task and associated executor
	private final LocalWatchService mWatchService = new LocalWatchService ();

	private final ExecutorService mWatchServiceExecutor = 
										createExecutor("WatchService", true);

	public LocalFileSystem(String rootPath) {
		super(FileSystemType.SOURCE_DISK, rootPath);
		construct();
	}
	
	public void start() {
		startWatchService();		
	}
	
	public void shutdown() {
		killWatchService();
	}
	
	public void startWatchService () {
		
		if (mWatchServiceExecutor.isShutdown())
			return;
		
		mWatchServiceExecutor.execute(mWatchService);
		mWatchServiceExecutor.shutdown();
	}

	public void killWatchService() {
		if (mWatchService.isRunning())
			mWatchService.cancel();
	}
	
	public ReadOnlyObjectProperty watchServiceState() {
		return mWatchService.stateProperty();
	}

	@Override
	protected void construct() {
		
	    LocalWatchPath.setRootPath (Paths.get(this.getRootPath()));
	    
		this.addedPaths().bind(mWatchService.addedPaths());
		this.removedPaths().bind(mWatchService.removedPaths());
		this.changedPaths().bind(mWatchService.changedPaths());
	}	
}