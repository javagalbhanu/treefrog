package com.buddyware.treefrog.local.config;

import java.nio.file.Path;

import com.buddyware.treefrog.local.model.LocalWatchPath;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class LocalTreeItem extends TreeItem <String>{
	
	private LocalWatchPath treeItemPath = null;
	
	public LocalTreeItem (String value) {
		super (value);
	}
	
	public LocalTreeItem (LocalWatchPath path) {
		super (path.getFullPath().toString());
		treeItemPath = path;
	}
	
	public LocalTreeItem (Path path) {
		super (path.toString());
		treeItemPath = new LocalWatchPath (path);
	}
	
	public LocalWatchPath getPath () {
		return treeItemPath;
	}
	
	public int getChildCount() {
		return this.getChildren().size();
	}
	
	public LocalTreeItem getChild(int index) {
		return (LocalTreeItem) (super.getChildren().get(index));
	}
}
