package com.buddyware.treefrog.filesystem;

import javafx.collections.ListChangeListener;

import com.buddyware.treefrog.BaseModel;

public interface IFileSystem {
/*
 * Interface for all file model classes
 */
	
	//startup call made before model is used
	void start();
	
	//shutdown call made before model is destroyed
	void shutdown();
	
	//serialize the model data for future session
	void serialize(String filepath);
	
	//deserialize the model data for current session
	void deserialize (String filepath);
	
	//property change functions
	void setOnPathsAdded (ListChangeListener <String> listener);
	void setOnPathsRemoved (ListChangeListener <String> listener);
	void setOnPathsChanged (ListChangeListener <String> listener);
	
	void removeOnPathsAdded (ListChangeListener <String> listener);
	void removeOnPathsRemoved (ListChangeListener <String> listener);
	void removeOnPathsChanged (ListChangeListener <String> listener);
}
