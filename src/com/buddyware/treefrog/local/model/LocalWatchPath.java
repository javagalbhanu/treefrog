package com.buddyware.treefrog.local.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import javafx.scene.control.TreeItem;

public class LocalWatchPath {

	/*
	 * A local watch path is a path which is linked from, or exists directly in
	 *the root watch path (user.home.bucketsync).
	 *
	 *Thus, when a path is passed in, it's relativized to the root watch path.
	 */
	
	private static Path mWatchPathRoot = Paths.get("/");
	
	private Path mFullPath = null;
	private Path mRelativePath = null;
	private String mRelativeName = "";
	private boolean mIsSymbolicLink = false;
	private Iterator <Path> pathIterator = null;
	
	
	public static void setRoot (Path path) {
		mWatchPathRoot = path;
	};
	
	public static Path getRoot () {
		return mWatchPathRoot;
	};
	
	public LocalWatchPath (String value) {
		mFullPath = Paths.get(value);
		mIsSymbolicLink = Files.isSymbolicLink(mFullPath);
	};
	
	public LocalWatchPath (Path targetPath) {
		mFullPath = targetPath;
		mIsSymbolicLink = Files.isSymbolicLink(mFullPath);
	};
	
	public Path getFullPath() { return mFullPath; };
	
	public String getRelativeName() {
		
		if (mRelativePath == null)
			relativizePath();
		
		return mRelativeName;
	}
	
	public boolean equals (LocalWatchPath target) {
		return mFullPath.equals(target.getFullPath());
	}
	
	public void resetIterator () {
		
		if (!mIsSymbolicLink)
			pathIterator = mFullPath.iterator();
		else {
			relativizePath();
			pathIterator = mRelativePath.iterator();
		}
	}
	
	public boolean hasNext() {
		if (pathIterator == null)
			resetIterator();
		
		return pathIterator.hasNext();
	}
	
	public String next() {
		
		if (pathIterator == null) {
			
			if (mIsSymbolicLink) {
				
System.out.println ("getting iterator for symlink " + mRelativePath.toString());				
				
				pathIterator = mRelativePath.iterator();
			}
			else {
System.out.println ("getting iterator for real path " + mFullPath.toString());				
				
				pathIterator = mFullPath.iterator();
			}
		}
		
		if (!pathIterator.hasNext())
			return "";
		
		return pathIterator.next().toString();
	}
	
	private void relativizePath () {

		if (mRelativePath != null)
			return;
		
		//symbolic links are relativized against their target's parent
		//actual directories are relativized against the watch path root
		
		if (mIsSymbolicLink) {
			try {
				mFullPath = Files.readSymbolicLink(mFullPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mRelativePath = mFullPath.getParent().relativize(mFullPath);
		}
		else
			mRelativePath = mWatchPathRoot.relativize(mFullPath);
		
		mRelativeName = mRelativePath.toString();
	};
}
