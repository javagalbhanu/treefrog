package com.buddyware.treefrog.view;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.buddyware.treefrog.model.filesystem.FileSystemType;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class CustomFxml {

	private static final Map <FileSystemType, String> mStyleMap = new HashMap<>();
	private static CustomFxml mInstance;
	
	private CustomFxml() {
		super();
		initStyleMap();
	}
	
	private void initStyleMap () {
			mStyleMap.put(FileSystemType.LOCAL_DISK, "fs-local-disk");
			mStyleMap.put(FileSystemType.AMAZON_S3, "fs-amazon-s3");
	}
	
	public void setStyle (Node n, FileSystemType f) {
System.out.println ("Setting style to " + mStyleMap.get(f))	;	
		n.getStyleClass().add(mStyleMap.get(f));
	}
	
	public static FXMLLoader load (URL resourcePath, Node parent) {
		
		FXMLLoader loader = new FXMLLoader(resourcePath);
		
		loader.setRoot(parent); 
		loader.setController(parent);
		
		try { 
			loader.load();
			 
		} catch (IOException exception) {
		    throw new RuntimeException(exception);
		}
		
		return loader;
	}
	
	public static CustomFxml getInstance() {
		
		if (mInstance == null)
			mInstance = new CustomFxml();
		
		return mInstance;
	}
}
