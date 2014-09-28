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

import javafx.scene.control.TreeItem;

public class LocalWatchPath implements Path {

	/*
	 * A local watch path is a path which is linked from, or exists directly in
	 *the root watch path (user.home.bucketsync).
	 *
	 *Thus, when a path is passed in, it's relativized to the root watch path.
	 */
	
	private static Path mWatchPathRoot = Paths.get("/");
	
	private Path mFullPath = null;
	private String mFullPathName = "";
	private Path mRelativePath = null;
	private String mRelativeName = "";
	private boolean mIsSymbolicLink = false;
	private Iterator <Path> pathIterator = null;
	
	public Path getFullPath() { return mFullPath; }
	public String getFullPathName() { return mFullPathName; }
	public boolean isSymbolicLink() { return mIsSymbolicLink; }	
	
	public static void setRootPath (Path path) {
		mWatchPathRoot = path;
	}
	
	public static Path getRootPath () {
		return mWatchPathRoot;
	}
	
	public LocalWatchPath (String value) {
		mFullPath = Paths.get(value);
		mFullPathName = mFullPath.toString();
		mIsSymbolicLink = Files.isSymbolicLink(mFullPath);
	}
	
	public LocalWatchPath (Path targetPath) {
		mFullPath = targetPath;
		mFullPathName = mFullPath.toString();
		mIsSymbolicLink = Files.isSymbolicLink(mFullPath);
	}
	
	public String getRelativeName() {
		
		if (mRelativePath == null)
			relativizePath();
		
		return mRelativeName;
	}
	
	public boolean equals (LocalWatchPath target) {
		return mFullPath.equals(target.getFullPath());
	}
	
	public void resetIterator () {
		
		relativizePath();
		pathIterator = mRelativePath.iterator();
	}
	
	public boolean hasNext() {
		if (pathIterator == null)
			resetIterator();
		
		return pathIterator.hasNext();
	}
	
	public String next() {
		
		if (pathIterator == null)
			resetIterator();
		
		if (!pathIterator.hasNext())
			return "";
		
		return pathIterator.next().toString();
	}
	
	public LocalWatchPath relativize (LocalWatchPath other) {
		return new LocalWatchPath (mFullPath.relativize(other.getFullPath()));
		
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
		else {
			mRelativePath = mWatchPathRoot.relativize(mFullPath);		
		}
		
		mRelativeName = mRelativePath.toString();
	}
	
	
	/*
	 *Default interface method implementations
	 */
	
	@Override
	public FileSystem getFileSystem() { return mFullPath.getFileSystem(); }

	@Override
	public boolean isAbsolute() { return mFullPath.isAbsolute(); }

	@Override
	public Path getFileName() { return mFullPath.getFileName();	}
	
	@Override
	public Path getParent() { return mFullPath.getParent(); }

	@Override
	public int getNameCount() { return mFullPath.getNameCount(); }

	@Override
	public Path getName(int index) { return mFullPath.getName(index); }
	
	@Override
	public Path subpath(int beginIndex, int endIndex) { 
		return mFullPath.subpath(beginIndex, endIndex);
	}
	
	@Override
	public boolean startsWith(Path other) { 
		return mFullPath.startsWith(other); 
	}
	
	@Override
	public boolean startsWith(String other) { 
		return mFullPath.startsWith(other);
	}
	
	@Override
	public boolean endsWith(Path other) { return mFullPath.endsWith(other);	}
	
	@Override
	public boolean endsWith(String other) { return mFullPath.endsWith(other); }
	
	@Override
	public Path normalize() { return mFullPath.normalize(); }
	
	@Override
	public Path resolve(Path other) { return mFullPath.resolve(other); }
	
	@Override
	public Path resolve(String other) { return mFullPath.resolve(other); }
	
	@Override
	public Path resolveSibling(Path other) {
		return mFullPath.resolveSibling(other);
	}
	
	@Override
	public Path resolveSibling(String other) {
		return mFullPath.resolveSibling(other);
	}
	
	@Override
	public Path relativize(Path other) { return mFullPath.relativize(other); }
	
	@Override
	public URI toUri() { return mFullPath.toUri(); }
	
	@Override
	public Path toAbsolutePath() { return mFullPath.toAbsolutePath(); }
	
	@Override
	public Path toRealPath(LinkOption... options) throws IOException {
		return mFullPath.toRealPath(options);
	}
	
	@Override
	public File toFile() { return mFullPath.toFile(); }
	
	@Override
	public WatchKey register(WatchService watcher, Kind<?>[] events,
			Modifier... modifiers) throws IOException {
		return mFullPath.register (watcher, events, modifiers);
	}
	
	@Override
	public WatchKey register(WatchService watcher, Kind<?>... events)
			throws IOException {
		return mFullPath.register (watcher, events);
	}
	
	@Override
	public Iterator<Path> iterator() { return mFullPath.iterator();	}
	
	@Override
	public int compareTo(Path other) { return mFullPath.compareTo (other); }
	
	@Override
	public Path getRoot() { return mFullPath.getRoot(); }
}
