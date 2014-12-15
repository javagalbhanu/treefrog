package com.buddyware.treefrog.filesystem.model.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
		
	    LocalWatchPath.setRootPath (this.getRootPath());
	    
		mChangedPaths.bind(mWatchService.changedPaths());
		
		mChangedPaths.addListener(new ListChangeListener <SyncPath> (){

			private boolean mFirstRun = true;
			
			@Override
			public void onChanged( 
				javafx.collections.ListChangeListener.Change<? extends SyncPath> 
								arg0) {
				
					if (mStartup) {
System.out.println("In startup");						
						if (!mFirstRun) {
System.out.println("Complete startup");						
							mStartup = false;
						}
					}
					
					if (mFirstRun) {
System.out.println("In first run");						
						mFirstRun = false;
					}
				}
			});
	}
	
	@Override
	public void putFile(SyncPath path) {

		if (path == null)
			return;
		
		if (path.getFile() == null)
			return;

		Path target = getRootPath().resolve(path.getRelativePath());
		
		//build out the path if it doesn't already exist
		try {
			Files.createDirectories(target.getParent());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Files.copy(path.getFile().toPath(), target, 
										StandardCopyOption.REPLACE_EXISTING, 
										StandardCopyOption.COPY_ATTRIBUTES);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean deleteFile(SyncPath path) {
		
		try {
			return Files.deleteIfExists(getRootPath().resolve(path.getRelativePath()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
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