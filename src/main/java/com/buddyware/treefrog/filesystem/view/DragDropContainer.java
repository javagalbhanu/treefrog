	package com.buddyware.treefrog.filesystem.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.input.DataFormat;
import javafx.util.Pair;

public class DragDropContainer implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1458406119115196098L;

	private final List <Pair<String, String> > mDataPairs = new ArrayList <Pair<String, String> > ();
	
	public static final DataFormat AddBinding = 
			new DataFormat("com.buddyware.treefrog.filesystem.view.FileSystemBinding.add");
	
	public static final DataFormat AddNode =
			new DataFormat("com.buddyware.treefrog.filesystem.view.FileSystemNode.add");
	
	public static final DataFormat MoveNode =
			new DataFormat("com.buddyware.treefrog.filesystem.view.FileSystemNode.move");
	
	public DragDropContainer () {
	}
	
	public void addData (String key, String value) {
		mDataPairs.add(new Pair<String, String>(key, value));		
	}
	
	public String getValue (String key) {
		
		for (Pair<String, String> data: mDataPairs) {
			
			if (data.getKey().equals(key))
				return (String) data.getValue();
				
		}
		
		return null;
	}
	
	public List <Pair<String, String> > getData () { return mDataPairs; }
}