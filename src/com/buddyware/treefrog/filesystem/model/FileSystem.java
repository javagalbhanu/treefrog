package com.buddyware.treefrog.filesystem.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;

import com.buddyware.treefrog.BaseModel;

public abstract class FileSystem extends BaseModel{
/*
 * Base class for FileSystem models
 */
	protected final SimpleListProperty <SyncPath> mChangedPaths;

	protected boolean mStartup = true;
	
	private final Path mRootPath;
	private final FileSystemType mType ;
	
	public FileSystem (FileSystemType type, String rootPath) {
		
		mType = type;
		mRootPath = Paths.get(rootPath);
		
		mChangedPaths = new SimpleListProperty <SyncPath> ();

 	}

	public abstract void start();
	public abstract void shutdown();
	protected abstract void construct();
	
	public FileSystemType getType() { return mType;	}
	
	//serialize the model data for future session
	public void serialize(String filepath) {}
	
	//deserialize the model data for current session
	public void deserialize (String filepath) {}

	public SimpleListProperty <SyncPath> pathsChanged() { return mChangedPaths; }

	public void setOnPathsChanged (ListChangeListener <SyncPath> changeListener) {
		mChangedPaths.addListener(changeListener);
	}

	public abstract Path getFile(String path);
	
	public abstract void cacheFile(SyncPath path);
	public abstract void modifyFile(SyncPath path);
	public abstract void createFile(SyncPath path);
	public abstract boolean deleteFile(SyncPath path);
	
	public boolean isStartingUp() { return mStartup; }
	
	public Path getRootPath() { return mRootPath; }
	
	//toString is used for binding hastable keys
	public String toString() { return mRootPath.toString();	}
}
