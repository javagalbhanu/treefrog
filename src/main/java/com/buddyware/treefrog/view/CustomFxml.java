package com.buddyware.treefrog.view;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.buddyware.treefrog.model.filesystem.FileSystemType;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.util.Pair;

public class CustomFxml {

	private static CustomFxml mInstance;
	
	private CustomFxml() {
		super();
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
