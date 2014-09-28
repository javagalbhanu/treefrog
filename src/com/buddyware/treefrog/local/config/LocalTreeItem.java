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
		super (path.getRelativeName());
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
	
	public LocalWatchPath getDescendant (LocalWatchPath path) {
		
		LocalWatchPath result = null;
		
		if (path.startsWith (mPath.getFullPathName()))
			result = mPath.relativize (path);

		if (result == null)
			return null;
		
		if (result.getFullPathName().isEmpty())
			return null;
			
		return result;
	}
	
	public boolean isAncestorOf (String pathName) {
		
		//ancestry based on matching path names
		return this.getValue().equals(pathName);
	}
	
	public boolean isAncestorOf (LocalWatchPath path) {
		//ancestry based on shared root paths
		return path.getFullPath().startsWith(mPath.getFullPathName());
	}
}
