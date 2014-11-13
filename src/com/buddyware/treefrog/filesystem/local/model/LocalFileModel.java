package com.buddyware.treefrog.filesystem.local.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;

import com.buddyware.treefrog.filesystem.FileSystemModel;

import javafx.beans.property.ReadOnlyObjectProperty;


public class LocalFileModel extends FileSystemModel {
	
	//watchService task and associated executor
	private final LocalWatchService mWatchService;

	private final ExecutorService mWatchServiceExecutor = 
										createExecutor("WatchService", true);

	public LocalFileModel() {
		
		super();
		
	    //rootpath points to the bucketsync directory in user's home
	    LocalWatchPath.setRootPath (
	    			Paths.get(System.getProperty("user.home") + "/bucketsync")
	    );
	    
		mWatchService = new LocalWatchService ();
		
		this.addedPaths().bind(mWatchService.addedPaths());
		this.removedPaths().bind(mWatchService.removedPaths());
		this.changedPaths().bind(mWatchService.changedPaths());
	}
	
	public void start() {
		startWatchService();		
	}
	
	public void shutdown() {
		killWatchService();
	}
	
	public Path getRootPath() {
		return LocalWatchPath.getRootPath();
	};
	
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
}