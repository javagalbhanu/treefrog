package com.buddyware.treefrog.local.config;

import java.nio.file.Path;

import com.buddyware.treefrog.local.model.LocalWatchPath;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class LocalTreeItem extends TreeItem <String>{
	
	private LocalWatchPath mPath = null;
	
	public LocalTreeItem (String value) {
		super (value);
	}
	
	public LocalTreeItem (LocalWatchPath path) {
		super (path.getName(path.getNameCount()-1).toString());
		mPath = path;
	}
	
	public LocalTreeItem (Path path) {
		super (path.toString());
		mPath = new LocalWatchPath (path);
	}
	
	public LocalWatchPath getPath () {
		return mPath;
	}
	
	public int getChildCount() {
		return this.getChildren().size();
	}
	
	public LocalTreeItem getChild(int index) {
		return (LocalTreeItem) (super.getChildren().get(index));
	}
	
	public LocalWatchPath getAsDescendant (LocalWatchPath path) {
		
		LocalWatchPath result = null;
		
		String treePath = mPath.toString();
		
		if (path.startsWith (treePath))
			result = mPath.relativize (path);

		if (result == null)
			return null;
		
		if (result.toString().isEmpty())
			return null;
			
		return result;
	}
}
