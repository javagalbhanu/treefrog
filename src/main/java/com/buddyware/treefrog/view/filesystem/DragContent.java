package com.buddyware.treefrog.view.filesystem;

import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;

public final class DragContent {

	public static final DataFormat AddBinding = 
			new DataFormat("com.buddyware.treefrog.view.filesystem.SyncBinding.add");
	
	public static final DataFormat AddNode =
			new DataFormat("com.buddyware.treefrog.view.filesystem.FileNode.add");
	
	public static final DataFormat MoveNode =
			new DataFormat("com.buddyware.treefrog.view.filesystem.FileNode.move");
	
	public static final void add (DragEvent e, DataFormat dataformat, 
									 String key, String data) {
		
		ClipboardContent content = new ClipboardContent();
		
		DragContainer container = 
				(DragContainer) e.getDragboard().getContent(dataformat);
		
		if (container == null)
			container = new DragContainer();
		
		container.addData(key, data);
		content.put(dataformat, container);
		
		e.getDragboard().setContent(content);		
	}
	
	public static final ClipboardContent create 
			(DataFormat dataformat, String key, String data) {
		
		ClipboardContent content = new ClipboardContent();
		DragContainer container = new DragContainer();
		
		container.addData (key, data);
		content.put(dataformat,  container);
		
		return content;		
	}
}
