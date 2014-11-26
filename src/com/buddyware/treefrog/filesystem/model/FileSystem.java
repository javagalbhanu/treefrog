package com.buddyware.treefrog.filesystem.model;

import java.io.File;

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
	private String mRootPath = null;
	private final FileSystemType mType ;
	
	public FileSystem (FileSystemType type, String rootPath) {
		mType = type;
		mRootPath = rootPath;
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
	public File getFile(String filepath) {
		System.out.println("getting file " + filepath);
		return null;
	}
	
	//saves a file
	public void putFile(File filepath) {
		System.out.println("putting file " + filepath);
	}
	
	public String getRootPath() {
		return mRootPath; 
	}
	
	//toString is used for binding hastable keys
	public String toString() {
		return mRootPath;
	}	
}
