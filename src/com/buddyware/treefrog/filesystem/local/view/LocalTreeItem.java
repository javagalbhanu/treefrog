package com.buddyware.treefrog.filesystem.local.view;

import java.nio.file.Path;

import com.buddyware.treefrog.filesystem.local.model.LocalWatchPath;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class LocalTreeItem extends TreeItem <String>{
	
	private LocalWatchPath mPath = null;
	
	public LocalTreeItem (String value) {
		super (value);
	}
	
	public LocalTreeItem (LocalWatchPath path) {
		super (path.getLastName());
		mPath = path;
	}
	
	public LocalTreeItem (Path path) {
		super ( path.toString());
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
		
		if (path.toCanonicalPath().startsWith (treePath))
			result = mPath.relativize (path);

		if (result != null) {
			if (result.toString().isEmpty())
				result = null;
		}
		
		return result;
	}
}
