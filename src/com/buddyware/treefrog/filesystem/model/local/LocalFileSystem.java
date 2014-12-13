package com.buddyware.treefrog.filesystem.model.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import com.buddyware.treefrog.filesystem.model.FileSystem;
import com.buddyware.treefrog.filesystem.model.FileSystemType;
import com.buddyware.treefrog.filesystem.model.SyncPath;
import com.buddyware.treefrog.util.utils;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ListChangeListener;

public class LocalFileSystem extends FileSystem {
	
	private final static String TAG = "LocalFileSystem";
	
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
	    
		mChangedPaths.bind(mWatchService.changedPaths());
		
		mChangedPaths.addListener(new ListChangeListener <SyncPath> (){

			private boolean mFirstRun = true;
			
			@Override
			public void onChanged( 
				javafx.collections.ListChangeListener.Change<? extends SyncPath> 
								arg0) {
				
					if (mStartup) {
						if (!mFirstRun) {
							mStartup = false;
						}
					}
					
					if (mFirstRun)
						mFirstRun = false;
				}
			});
	}
	
	private void addDirectory (Path target) {
		System.out.println(TAG + "addDirectory: " + target.toString());
	}
	
	private void addFile (Path target) {
		System.out.println(TAG + "addFile: " + target.toString());
	}
	
	
	@Override
	public void putFile(SyncPath target) {

		if (target == null)
			return;
		
		if (target.getFile() == null)
			return;
		
		//build out the path if it doesn't already exist
		Path newPath = getRootPath().resolve(target.getPath());
		
		System.out.println(TAG + ".putFile(): resolved " + newPath.toString());
		
	}
	
	@Override
	public Path getFile(String path) {
		
		//skip empty strings
		if (path.isEmpty())
			return null;

		Path targetPath = Paths.get(path);
		
		//do not sync symbolic links
		if (Files.isSymbolicLink(targetPath))
			return null;
		
		File targetFile = new File(path);

		//do not process if the path is a directory
		if (!targetFile.isFile())
			return null;
		
		//relativize target against the root path.
		//if it doesn't have the root path, it's part of a
		//symbolic link.  Find that link and relative against it
		if (targetPath.startsWith(getRootPath().toString()))
			targetPath = getRootPath().relativize(targetPath);
		else {
			
			DirectoryStream.Filter<Path> filter = 
			new DirectoryStream.Filter<Path>() {
			
				public boolean accept(Path file) throws IOException {
					
					return (Files.isSymbolicLink(file));
				}
			};

			ArrayList<Path> paths = utils.getFiles(targetPath, filter);
			
			for (Path p: paths) {
				if (targetPath.startsWith(p))
					targetPath = p.relativize(targetPath);
			}
		}
		
		
		return targetPath;
	}

}