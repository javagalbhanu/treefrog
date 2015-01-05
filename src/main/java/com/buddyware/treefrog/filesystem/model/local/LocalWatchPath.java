package com.buddyware.treefrog.filesystem.model.local;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class LocalWatchPath {

	/*
	 * A local watch path is a path which is linked from, or exists directly in
	 * the root watch path (user.home.bucketsync).
	 * 
	 * Thus, when a path is passed in, it's relativized to the root watch path.
	 */

	private static Path mWatchPathRoot = Paths.get("/");

	private Path mPath = null;
	private Iterator<Path> pathIterator = null;
	private String mPathName = "";

	public static void setRootPath(Path path) {
		mWatchPathRoot = path;
	}

	public static void setRootPath(String path) {
		mWatchPathRoot = Paths.get(path);
	}

	public static Path getRootPath() {
		return mWatchPathRoot;
	}

	public LocalWatchPath(String value) {
		mPathName = value;
	}

	public LocalWatchPath(Path targetPath) {
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

	public String getLastName() {

		if (mPath == null)
			resolvePath();

		if (mPath == null)
			return "";

		return mPath.getName(mPath.getNameCount() - 1).toString();
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

	public Path toCanonicalPath() {

		if (mPath == null)
			resolvePath();

		return mPath;
	}

	public void resetIterator() {

		if (mPath == null)
			resolvePath();

		try {
			pathIterator = mPath.iterator();
		} catch (NullPointerException e) {
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
		} catch (NullPointerException e) {
			e.printStackTrace();
			return "";
		}

		return pathIterator.next().toString();
	}

	public LocalWatchPath relativizeToParent() {

		LocalWatchPath result = null;

		if (mPath == null)
			resolvePath();

		try {
			result = new LocalWatchPath(mPath.getParent().relativize(mPath));
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		return result;
	}

	public boolean isAncestorOf(LocalWatchPath target) {
		return target.toCanonicalPath().startsWith(mPath.toString());
	}

	public boolean equals(LocalWatchPath target) {
		return mPath.equals(target.toCanonicalPath());
	}

	public boolean equals(Path target) {
		return mPath.equals(target);
	}

	public LocalWatchPath relativize(LocalWatchPath other) {

		LocalWatchPath result = null;

		if (mPath == null)
			resolvePath();

		try {
			result = new LocalWatchPath(mPath.relativize(other
					.toCanonicalPath()));
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		return result;
	}
}
