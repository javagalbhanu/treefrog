package com.buddyware.treefrog.filesystem.model;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;

import com.buddyware.treefrog.BaseModel;

public abstract class FileSystem extends BaseModel{
/*
 * Base class for FileSystem models
 */
	protected final SimpleListProperty <String> mPathsAdded;
	protected final SimpleListProperty <String> mPathsRemoved;
	protected final SimpleListProperty <String> mPathsChanged;
	private final Path mRootPath;
	private final FileSystemType mType ;
	
	public FileSystem (FileSystemType type, String rootPath) {
		mType = type;
		mRootPath = Paths.get(rootPath);
		mPathsAdded = new SimpleListProperty <String> ();
		mPathsRemoved = new SimpleListProperty <String> ();
		mPathsChanged = new SimpleListProperty <String> ();
 	}

	public abstract void start();
	public abstract void shutdown();
	protected abstract void construct();
	
	public FileSystemType getType() {
		return mType;
	}
	
	//serialize the model data for future session
	public void serialize(String filepath) {}
	
	//deserialize the model data for current session
	public void deserialize (String filepath) {}
	
	public SimpleListProperty <String> addedPaths() {
	
		return mPathsAdded;
	}

	public SimpleListProperty <String> removedPaths() {
		
		return mPathsRemoved;
	}

	public SimpleListProperty <String> changedPaths() {
		
		return mPathsChanged;
	}
	
	public void setOnPathsAdded (ListChangeListener <String> changeListener) {
		mPathsAdded.addListener(changeListener);
	}

	public void setOnPathsRemoved (ListChangeListener <String> listener) {
		mPathsRemoved.addListener(listener);
	}
	
	public void removeOnPathsChanged (ListChangeListener <String> listener) {
		mPathsChanged.addListener(listener);
	}
	
	//retrieves a file
	public abstract SyncPath getFile(String path);
	
	//saves a file
	public abstract void putFile(SyncPath target);
	
	public Path getRootPath() {
		return mRootPath; 
	}
	
	//toString is used for binding hastable keys
	public String toString() {
		return mRootPath.toString();
	}	
}
