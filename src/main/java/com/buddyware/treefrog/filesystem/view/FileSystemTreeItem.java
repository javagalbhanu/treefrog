package com.buddyware.treefrog.filesystem.view;

import java.nio.file.Path;

import com.buddyware.treefrog.filesystem.model.local.LocalWatchPath;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class FileSystemTreeItem extends TreeItem<String> {

	private LocalWatchPath mPath = null;

	public FileSystemTreeItem(String value) {
		super(value);
	}

	public FileSystemTreeItem(LocalWatchPath path) {
		super(path.getLastName());
		mPath = path;
	}

	public FileSystemTreeItem(Path path) {
		super(path.toString());
		mPath = new LocalWatchPath(path);
	}

	public LocalWatchPath getPath() {
		return mPath;
	}

	public int getChildCount() {
		return this.getChildren().size();
	}

	public FileSystemTreeItem getChild(int index) {
		return (FileSystemTreeItem) (super.getChildren().get(index));
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
