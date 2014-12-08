package com.buddyware.treefrog.filesystem.model.local;

import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;

import com.buddyware.treefrog.filesystem.model.FileSystem;
import com.buddyware.treefrog.filesystem.model.FileSystemType;

import javafx.beans.property.ReadOnlyObjectProperty;


public class LocalFileSystem extends FileSystem {
	
	//watchService task and associated executor
	private final LocalWatchService mWatchService;

	private final ExecutorService mWatchServiceExecutor = 
										createExecutor("WatchService", true);

	public LocalFileSystem(FileSystemType type, String rootPath) {
		
		super(type, rootPath);
		
		mWatchService = new LocalWatchService(rootPath);
		
		construct();
	}
	
	public void start() {
System.out.println("Starting watch service on path " + this.getRootPath());		
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
System.out.println ("Constructing " + this.getRootPath());		
	    LocalWatchPath.setRootPath (this.getRootPath());
	    
		this.addedPaths().bind(mWatchService.addedPaths());
		this.removedPaths().bind(mWatchService.removedPaths());
		this.changedPaths().bind(mWatchService.changedPaths());
	}
	
}