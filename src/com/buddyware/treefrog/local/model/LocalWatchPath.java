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

public class LocalWatchPath implements Path {

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
		mPath = Paths.get(value);
		mPathName = mPath.toString();
		resolveSymbolicLink();
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
	
	public Path toCanonicalPath () { return mPath; }
	public void resetIterator () {
		pathIterator = mPath.iterator();
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
	
	public LocalWatchPath relativizeToParent () {
		return new LocalWatchPath (mPath.getParent().relativize(mPath));
	}
	
	public LocalWatchPath relativize (LocalWatchPath other) {
		return new LocalWatchPath (mPath.relativize (other.toCanonicalPath()));
	}
	/*
	 *Default interface method implementations
	 */
	@Override
	public String toString() { return mPathName; } 
	
	@Override
	public FileSystem getFileSystem() { return mPath.getFileSystem(); }

	@Override
	public boolean isAbsolute() { return mPath.isAbsolute(); }

	@Override
	public Path getFileName() { return mPath.getFileName();	}
	
	@Override
	public Path getParent() { return mPath.getParent(); }

	@Override
	public int getNameCount() { return mPath.getNameCount(); }

	@Override
	public Path getName(int index) { return mPath.getName(index); }
	
	@Override
	public Path subpath(int beginIndex, int endIndex) { 
		return mPath.subpath(beginIndex, endIndex);
	}
	
	@Override
	public boolean startsWith(Path other) { 
		return mPath.startsWith(other); 
	}
	
	@Override
	public boolean startsWith(String other) { 
		return mPath.startsWith(other);
	}
	
	@Override
	public boolean endsWith(Path other) { return mPath.endsWith(other);	}
	
	@Override
	public boolean endsWith(String other) { return mPath.endsWith(other); }
	
	@Override
	public Path normalize() { return mPath.normalize(); }
	
	@Override
	public Path resolve(Path other) { return mPath.resolve(other); }
	
	@Override
	public Path resolve(String other) { return mPath.resolve(other); }
	
	@Override
	public Path resolveSibling(Path other) {
		return mPath.resolveSibling(other);
	}
	
	@Override
	public Path resolveSibling(String other) {
		return mPath.resolveSibling(other);
	}
	
	@Override
	public Path relativize(Path other) { return mPath.relativize(other); }
	
	@Override
	public URI toUri() { return mPath.toUri(); }
	
	@Override
	public Path toAbsolutePath() { return mPath.toAbsolutePath(); }
	
	@Override
	public Path toRealPath(LinkOption... options) throws IOException {
		return mPath.toRealPath(options);
	}
	
	@Override
	public File toFile() { return mPath.toFile(); }
	
	@Override
	public WatchKey register(WatchService watcher, Kind<?>[] events,
			Modifier... modifiers) throws IOException {
		return mPath.register (watcher, events, modifiers);
	}
	
	@Override
	public WatchKey register(WatchService watcher, Kind<?>... events)
			throws IOException {
		return mPath.register (watcher, events);
	}
	
	@Override
	public Iterator<Path> iterator() { return mPath.iterator();	}
	
	@Override
	public int compareTo(Path other) { return mPath.compareTo (other); }
	
	@Override
	public Path getRoot() { return mPath.getRoot(); }
}
