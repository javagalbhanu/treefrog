package com.buddyware.treefrog.filesystem.model.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import com.buddyware.treefrog.filesystem.model.FileSystem;
import com.buddyware.treefrog.filesystem.model.FileSystemType;
import com.buddyware.treefrog.filesystem.model.SyncPath;
import com.buddyware.treefrog.filesystem.model.SyncType;
import com.buddyware.treefrog.util.utils;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ListChangeListener;

public class LocalFileSystem extends FileSystem {
	
	private final static String TAG = "LocalFileSystem";
	
	//watchService task and associated executor
	private final LocalWatchService mWatchService;

	private final ExecutorService mWatchServiceExecutor = 
										createExecutor("WatchService", true);

	private final Path mCachePath;
	
	public LocalFileSystem(FileSystemType type, String rootPath) {
		
		super(type, rootPath);
		
		mWatchService = new LocalWatchService(rootPath);
		mCachePath = getRootPath().resolve(".cache");
		
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
		initFileCache();
		
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
					
					if (mFirstRun) {
						
						mFirstRun = false;
					}
				}
			});
	}

	
	private void initFileCache() {
		
		//create the path if it does not exist, otherwise, ensure it is empty
		if (!Files.exists(mCachePath))
			try {
				Files.createDirectory(mCachePath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else {
			for(File file: mCachePath.toFile().listFiles()) 
				file.delete();
		}
			
	}	
	@Override
	public synchronized void cacheFile(SyncPath path) {
		
		if (path.getFile() == null)
			return;
		
		//create the cache target filename based on the source path's hash code
		Path target = mCachePath.resolve(Integer.toString(path.hashCode()));
		Path source = path.getPath();
		
		try {

			if (path.getSyncType() == SyncType.SYNC_CREATE) {
				
				//create a path representing the final location on the target
				//filesystem
				File exTarget = getRootPath().resolve(path.getRelativePath()).toFile();
				
				//abort if the file exists on the target system, has the same
				//size and same time-date stamp.  
				// TODO Replace with md5 checksum comparison
				if (exTarget.exists()) {
			
					if (exTarget.length() == source.toFile().length() &&
						exTarget.lastModified() == source.toFile().lastModified())
							return;
				}

				// Saving original syncpath rather than new target.
				// This saves the relative directory structure.
				mWatchService.cachePath(path);
				
				//copies the source path saved in the syncpath object to the
				//cache target location (filename = sourcepath hashcode)
				Files.copy(path.getPath(), target, StandardCopyOption.COPY_ATTRIBUTES);
			}
			
			//TODO need to determine what attributes are compared to decide if
			//file has been modified
			else {

				mWatchService.cachePath(path);
				Files.copy(path.getPath(), target, 
					StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.COPY_ATTRIBUTES);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private synchronized void dumpFileMeta (Path path) {
	
		File f = path.toFile();
		
System.out.println("Path: " + path +"\n\t Last Modified: " + f.lastModified() + "\n\t Size: " + f.length() );
		
	}
	
	@Override
	public synchronized void modifyFile(SyncPath path) {
	
		//abort for null values
		if (path == null)
			return;
		
		if (path.getFile() == null)
			return;
		
		Path target = getRootPath().resolve(path.getRelativePath());
		
		//abort for modifying a file that doesn't exist
		if (!Files.exists(target))
			return;
		
		
	}
	
	@Override
	public synchronized void createFile(SyncPath path) {

		//abort for null values
		if (path == null)
			return;
		
		if (path.getFile() == null)
			return;

		
		Path target = getRootPath().resolve(path.getRelativePath());

		//abort for creating a file that already exists
		if (Files.exists(target))
			return;
				
		//build out the path if it doesn't already exist
		try {
			Files.createDirectories(target.getParent());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			//mWatchService.addSkippedWatch(target);
			Files.copy(path.getPath(), target, StandardCopyOption.COPY_ATTRIBUTES);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public synchronized boolean deleteFile(SyncPath path) {
		
		try {
			return Files.deleteIfExists(getRootPath().resolve(path.getRelativePath()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	@Override
	public synchronized Path getFile(String path) {
		
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