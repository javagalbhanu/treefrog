package com.buddyware.treefrog.local.model;

import java.io.IOException;
import java.nio.file.Path;

import javafx.scene.control.TreeItem;

public class LocalTreeItem extends TreeItem <String>{
	
	private final Path treeItemPath;
	
	LocalTreeItem (Path path) {
		
		try {
			this.setValue(path.toRealPath().toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.treeItemPath = path;
	}
	
	public boolean pathEquals (Path target) {
		return treeItemPath.endsWith(target);
	}
}
