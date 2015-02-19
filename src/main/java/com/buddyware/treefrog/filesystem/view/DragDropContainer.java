package com.buddyware.treefrog.filesystem.view;

import java.io.Serializable;

import javafx.scene.input.DataFormat;

public class DragDropContainer implements Serializable{
	
	private String mSource;
	private String mTarget;
	
	public static final DataFormat BindingDataFormat = 
			new DataFormat("com.buddyware.treefrog.filesystem.view.FileSystemBinding");
	
	public DragDropContainer () {
	}
	
	public void setSource (String rhs) { mSource = rhs;	}
	public void setTarget (String rhs) { mTarget = rhs; }
	
	public String getSource () { return mSource; }
	public String getTarget () { return mTarget; }
}