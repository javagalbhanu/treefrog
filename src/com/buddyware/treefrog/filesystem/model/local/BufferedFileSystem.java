package com.buddyware.treefrog.filesystem.model.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.ExecutorService;

import com.buddyware.treefrog.filesystem.model.FileSystem;
import com.buddyware.treefrog.filesystem.model.FileSystemType;
import com.buddyware.treefrog.filesystem.model.SyncPath;
import com.buddyware.treefrog.util.utils;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;


public class BufferedFileSystem extends FileSystem {
	
	private final static String TAG = "BufferedFileSystem";
	
	//watchService task and associated executor
	private final BufferedWatchServiceTask mWatchService;

	private final ExecutorService mWatchServiceExecutor = 
										createExecutor("WatchService", true);
	
	private final ListProperty <SyncPath> mBufferPaths = 
										new SimpleListProperty <SyncPath> ();
	
	private final List <SyncPath> mFileCommits = new ArrayList <SyncPath> ();
	private final List <SyncPath> mFilePublishes = new ArrayList <SyncPath> ();
	
	public BufferedFileSystem(FileSystemType type, String rootPath) {
		
		super(type, rootPath);
		mWatchService = new BufferedWatchServiceTask(rootPath);
		construct();
	}
	
	public void start() {
System.out.println(TAG + ".start()");
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

	@Override
	protected void construct() {
		
		//TODO eliminate LocalWatchPath class references
	    LocalWatchPath.setRootPath (this.getRootPath());

	    //TODO Move watchservice / buffer code to FileSystem base as all 
	    //filesystems will at least need the .cache buffer to function
	    
		//Use a local property in the file model to bind to the watchservice
		//model to allow executoin on the model's thread for list change updates
		mBufferPaths.bind(mWatchService.changedPaths());
		
		mBufferPaths.addListener(new ListChangeListener <SyncPath> (){
			
			/*
			*When the watchservice notifies us of a path change:
			*
			*	Changes on sync paths: publish to the outside.
			*	Changes on .cache paths: update the sync path.  
			*/
			
			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends SyncPath> c) {
System.out.println(TAG + ".onChanged() processing WatchService events");			
				mFilePublishes.clear();
				mFileCommits.clear();
				
				//iterate the sync paths, moving cached files to their final destination
				//or publishing queued file changes to the outside
				for (SyncPath p: c.getList()) {
					
					if (p.getPath().startsWith(getCachePath()))
						mFileCommits.add(p);
					else 
						mFilePublishes.add(p);
				}
				
				commitFiles();
				publishFiles();
				
			}
			
		});		
	}

	private void commitFiles () {
		/*
		 * Executes a task to move a cached file
		 * from it's source to it's destination
		 */
		if (mFileCommits.isEmpty()) return;
System.out.println (TAG + ".commitFiles() " + mFileCommits.size());

		for (SyncPath p:mFileCommits) {
			
			if (p.getFile() == null)
				continue;

			//retrieve the original sync path from the original source using
			//the cached file's filename to provide the hashcode key
			SyncPath cache_source = takeCachedFile (Integer.parseInt(p.getFile().getName()));

			//skip if the original path is not present - it's no longer a valid
			//cached file
			if (cache_source == null)
				continue;
			
System.out.println (TAG + ".commitFiles() " + p.getPath());
			
			//resolve the cached source's relative path against the target's
			//root path to make the final copy
			Path target = getRootPath().resolve(cache_source.getRelativePath());

			if (!Files.exists(target.getParent()))
					target.getParent().toFile().mkdirs();
			
			moveFile (p.getPath(), target,
						StandardCopyOption.REPLACE_EXISTING, 
						StandardCopyOption.ATOMIC_MOVE);
		}
	}
	
	private void publishFiles () {
		/*
		 * Add a syncpath to the changed paths property for
		 * application-wide notification of file changes 
		 * (UI updates, binding updates, etc)
		 */
		if (mFilePublishes.isEmpty()) return;

		mChangedPaths.setAll(mFilePublishes);
		mFilePublishes.clear();
	}
	
	@Override
	public synchronized boolean deleteFiles(List <SyncPath> paths) {
		
		boolean success = true;
		for (SyncPath p:paths) {
		
			try {
System.out.println(TAG + ".deleteFiles() DELETING " + p.getPath() +"\n\tfrom " + getRootPath());				
				success = success & Files.deleteIfExists(
								getRootPath().resolve(p.getRelativePath()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return success;
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