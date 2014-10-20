package com.buddyware.treefrog.local.model;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

public class LocalWatchPath {

	/*
	 * A local watch path is a path which is linked from, or exists directly in
	 *the root watch path (user.home.bucketsync).
	 *
	 *Thus, when a path is passed in, it's relativized to the root watch path.
	 */
	
	private static Path mWatchPathRoot = Paths.get("/");
	
	private Path mPath = null;
	private Iterator <Path> pathIterator = null;
	private String mPathName = "";
	
	public static void setRootPath (Path path) { mWatchPathRoot = path;	}
	public static Path getRootPath () {	return mWatchPathRoot;	}
	
	public LocalWatchPath (String value) {
		mPathName = value;
	}
	
	public LocalWatchPath (Path targetPath) {
		mPath = targetPath;
		mPathName = targetPath.toString();
		resolveSymbolicLink();
	}
	
	private void resolveSymbolicLink() {
		if (Files.isSymbolicLink(mPath))
			try {
				mPath = Files.readSymbolicLink(mPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public String toString() {
		return mPathName;
	}
	
	private boolean resolvePath() {
		if (mPath != null)
			return true;
		
		if (mPathName.isEmpty())
			return false;
		
		mPath = Paths.get(mPathName);
		
		return (mPath != null);
	}
	
	public Path toCanonicalPath () {
		
		if (mPath == null)
			resolvePath();
		
		return mPath; 
	}
	
	public void resetIterator () {
		
		if (mPath == null)
			resolvePath();
		
		try {
			pathIterator = mPath.iterator();
		} catch ( NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasNext() {
		
		boolean success = false;
		
		if (pathIterator == null)
			resetIterator();
		
		try {
			success = pathIterator.hasNext();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		return success;
	}
	
	public String next() {
		
		boolean success = false;
		
		if (pathIterator == null)
			resetIterator();
		
		try {
			success = pathIterator.hasNext();
		} catch ( NullPointerException e) {
			e.printStackTrace();
			return "";
		}
		
		return pathIterator.next().toString();
	}
	
	public LocalWatchPath relativizeToParent () {
		
		LocalWatchPath result = null;
		
		if (mPath == null)
			resolvePath();
		
		try {
			result = new LocalWatchPath (mPath.getParent().relativize(mPath));
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public LocalWatchPath relativize (LocalWatchPath other) {
		
		LocalWatchPath result = null;
		
		if (mPath == null)
			resolvePath();
		
		try {
			result = 
				new LocalWatchPath (mPath.relativize (other.toCanonicalPath()));
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
