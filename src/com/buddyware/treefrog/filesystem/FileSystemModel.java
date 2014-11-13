package com.buddyware.treefrog.filesystem;

import java.io.File;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;

import com.buddyware.treefrog.BaseModel;

public class FileSystemModel extends BaseModel{
/*
 * Base class for FileSystem models
 */
	private final SimpleListProperty <String> mPathsAdded;
	private final SimpleListProperty <String> mPathsRemoved;
	private final SimpleListProperty <String> mPathsChanged;
	
	public FileSystemModel () {
		mPathsAdded = new SimpleListProperty <String> ();
		mPathsRemoved = new SimpleListProperty <String> ();
		mPathsChanged = new SimpleListProperty <String> ();
 	}

	//serialize the model data for future session
	void serialize(String filepath) {}
	
	//deserialize the model data for current session
	void deserialize (String filepath) {}
	
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
	
	void removeOnPathsChanged (ListChangeListener <String> listener) {
		mPathsChanged.addListener(listener);
	}
	
	//retrieves a file
	File getFile(String filepath) {
		return null;
	}
	
	//saves a file
	void putFile(File filepath) {}
}
