package com.buddyware.treefrog.filesystem.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

import com.buddyware.treefrog.BaseModel;
import com.buddyware.treefrog.ThreadPool;

public abstract class FileSystem extends BaseModel{
/*
 * Base class for FileSystem models
 */
	protected final SimpleListProperty <SyncPath> mChangedPaths;

	protected boolean mStartup = true;
	
	private final Path mRootPath;
	private final FileSystemType mType ;
	
	private final Path mCachePath;
	private final Map <Integer, SyncPath> mCachedPaths = 
											new HashMap <Integer, SyncPath> ();
	
	public FileSystem (FileSystemType type, String rootPath) {
		
		mType = type;
		mRootPath = Paths.get(rootPath);
		mCachePath = mRootPath.resolve(".cache");
		mChangedPaths = new SimpleListProperty <SyncPath> (
										FXCollections.observableArrayList());

		//create the .cache path if it does not exist, 
		//otherwise, ensure it is empty
		if (!Files.exists(mCachePath))
			try {
				Files.createDirectory(mCachePath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		else {
			for(File file: mCachePath.toFile().listFiles()) 
				file.delete();
		}		
 	}

	public abstract void start();
	public abstract void shutdown();
	protected abstract void construct();
	
	public abstract Path getFile(String path);
	public abstract boolean deleteFiles(List <SyncPath> paths);
	
	public FileSystemType getType() { return mType;	}

	public SimpleListProperty <SyncPath> pathsChanged() { return mChangedPaths; }
	public void setOnPathsChanged (ListChangeListener <SyncPath> changeListener) {
		mChangedPaths.addListener(changeListener);
	}
	
	public Path getRootPath() { return mRootPath; }
	public Path getCachePath() { return mCachePath; }
	
	public synchronized void addOrUpdateCachedPath (SyncPath path) {
		
		SyncPath p = mCachedPaths.get(path.hashCode());
		
		if (p == null) {
			path.setQueuedTime(System.currentTimeMillis());
			mCachedPaths.put(path.hashCode(), path);
		}
		else
			p.setQueuedTime(System.currentTimeMillis());
	}
	
	public synchronized SyncPath takeCachedFile (Integer hash_code) {
		return mCachedPaths.remove(hash_code);
	}
	
	public synchronized SyncPath getCachedFile (Integer hash_code) {
		return mCachedPaths.get(hash_code);
	}
	
	public synchronized void putFiles (List <SyncPath> paths) {
		
		if (paths.isEmpty())
			return;
		
		for (SyncPath p: paths) {
			
			if (p.getFile() == null)
				continue;
			
			if (!p.getFile().exists())
				continue;
			
			//create the cache target filename based on the source path's hash code
			Path target = mCachePath.resolve(Integer.toString(p.hashCode()));
			Path source = p.getPath();
						
			//update the cache table, adding new paths or updating the current
			//path's queuetime
			addOrUpdateCachedPath (p);
			
			if (p.getSyncType() == SyncType.SYNC_CREATE) {
				
				//create a path representing the final location on the target
				//filesystem
				File exTarget = getRootPath().resolve(p.getRelativePath()).toFile();
				
				//abort if the file exists on the target system, has the same
				//size and same time-date stamp.  
				// TODO Replace with md5 checksum comparison
				if (exTarget.exists()) {
			
					if (exTarget.length() == source.toFile().length() &&
						exTarget.lastModified() == source.toFile().lastModified())
							return;
				}

				//copies the source path saved in the syncpath object to the
				//cache target location (filename = sourcepath hashcode)
				copyFile(p.getPath(), target, StandardCopyOption.COPY_ATTRIBUTES);
			}
			
			//TODO need to determine what attributes are compared to decide if
			//file has been modified
			else {

				copyFile(p.getPath(), target, 
					StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.COPY_ATTRIBUTES);
			}
		}
	}
	
	public String toString() { return mRootPath.toString();	}
	
	protected void copyFile (final Path source, final Path target, 
												final CopyOption... options) {
		
		Runnable r = () -> {
			try {
				Files.copy(source, target, options);
			} catch (IOException e) {
				e.printStackTrace();
			}			
		};
		
		ThreadPool.getInstance().executeCachedTask(r);
	}
	
	protected void moveFile (final Path source, final Path target,
												final CopyOption... options) {
		Runnable r = () -> {
			try {
				Files.move(	source, target, options);
			} catch (IOException e) {
				e.printStackTrace();
			} 
		};
		
		ThreadPool.getInstance().executeCachedTask(r);
	}
}
