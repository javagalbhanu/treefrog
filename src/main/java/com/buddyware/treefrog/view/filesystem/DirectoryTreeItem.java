package com.buddyware.treefrog.view.filesystem;

import java.nio.file.Path;

import com.buddyware.treefrog.model.filesystem.local.LocalWatchPath;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class DirectoryTreeItem extends TreeItem<String> {

	private LocalWatchPath mPath = null;

	public DirectoryTreeItem(String value) {
		super(value);
	}

	public DirectoryTreeItem(LocalWatchPath path) {
		super(path.getLastName());
		mPath = path;
	}

	public DirectoryTreeItem(Path path) {
		super(path.toString());
		mPath = new LocalWatchPath(path);
	}

	public LocalWatchPath getPath() {
		return mPath;
	}

	public int getChildCount() {
		return this.getChildren().size();
	}

	public DirectoryTreeItem getChild(int index) {
		return (DirectoryTreeItem) (super.getChildren().get(index));
	}

	public LocalWatchPath getAsDescendant(LocalWatchPath path) {

		LocalWatchPath result = null;

		String treePath = mPath.toString();

		if (path.toCanonicalPath().startsWith(treePath))
			result = mPath.relativize(path);

		if (result != null) {
			if (result.toString().isEmpty())
				result = null;
		}

		return result;
	}
}
