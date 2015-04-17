package com.buddyware.treefrog.filesystem.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point2D;

import com.buddyware.treefrog.BaseModel;
import com.buddyware.treefrog.ThreadPool;
import com.buddyware.treefrog.filesystem.FileSystemType;

public abstract class FileSystem extends BaseModel {
	/*
	 * Base class for FileSystem models
	 */
	protected final SimpleListProperty<SyncPath> mChangedPaths = 
			new SimpleListProperty<SyncPath>(
					FXCollections.observableArrayList()
					);

	private final Map<Integer, SyncPath> mCachedPaths = 
			new HashMap<Integer, SyncPath>();

	protected boolean mStartup = true;

	private Path mRootPath = null;
	
	private FileSystemType mType;
	
	private Point2D mLayoutPoint = new Point2D(-1.0, -1.0);

	private Path mCachePath = null;

	
	public FileSystem () {
		
	}
	
	public FileSystem(FileSystemType type, String rootPath) {

		mType = type;
		
		if (rootPath == null)
    		return;
		
	}

	public abstract void start();

	public abstract void shutdown();

	protected abstract void construct();

	public abstract Path getFile(String path);

	public abstract boolean deleteFiles(List<SyncPath> paths);

	public Point2D getLayoutPoint() { return mLayoutPoint; }
	
	public void setLayoutPosition(double x, double y) { 
		mLayoutPoint = new Point2D(x, y); 
	}
	
	public FileSystemType getType() {
		return mType;
	}

	public SimpleListProperty<SyncPath> pathsChanged() {
		return mChangedPaths;
	}

	public void setOnPathsChanged(ListChangeListener<SyncPath> changeListener) {
		mChangedPaths.addListener(changeListener);
	}
	
	public void setRootPath (String path) {
		
		mRootPath = Paths.get(path);
		mCachePath = mRootPath.resolve(".cache");

		// create the .cache path if it does not exist,
		// otherwise, ensure it is empty
		if (!Files.exists(mCachePath))
			try {
				Files.createDirectory(mCachePath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		else {
			for (File file : mCachePath.toFile().listFiles())
				file.delete();
		}
		
	}
	
	public Path getRootPath() { return mRootPath; }

	public Path getCachePath() { return mCachePath;	}
	
	public String getProperty (FileSystemProperty propName) {
	
		switch (propName) {
			
		case ID:
			return getId();
			
		case NAME:
			return getName();
			
		case LAYOUT_X:
			return Double.toString(getLayoutPoint().getX());
			
		case LAYOUT_Y:
			return Double.toString(getLayoutPoint().getY());
			
		case TYPE:
			return getType().toString();

		case PATH:
			
			Path p = getRootPath();
			
			if (p == null)
				return "";

			return p.toString();
		
		default:
			break;
		}
		
		return null;
	}
	
	public void setProperty (FileSystemProperty propName, String value) {
		
		switch(propName) {

		case ID:
			setId (value);
		break;
		
		case NAME:
			setName (value);
		break;
		
		case LAYOUT_X:
			setLayoutPosition (Double.valueOf(value), mLayoutPoint.getY());
		break;
		
		case LAYOUT_Y:
			setLayoutPosition (mLayoutPoint.getX(), Double.valueOf(value));
		break;
				
		case PATH:
			setRootPath(value);
		break;

		case TYPE:
			
			if (mType == null)
				mType = FileSystemType.valueOf(value);
		break;
		
		default:
		break;
		}
	}

	public synchronized void addOrUpdateCachedPath(SyncPath path) {

		SyncPath p = mCachedPaths.get(path.hashCode());

		if (p == null) {
			path.setQueuedTime(System.currentTimeMillis());
			mCachedPaths.put(path.hashCode(), path);
		} else
			p.setQueuedTime(System.currentTimeMillis());
	}

	public synchronized SyncPath takeCachedFile(Integer hash_code) {
		return mCachedPaths.remove(hash_code);
	}

	public synchronized SyncPath getCachedFile(Integer hash_code) {
		return mCachedPaths.get(hash_code);
	}

	public synchronized void putFiles(List<SyncPath> paths) {

		if (paths.isEmpty())
			return;

		for (SyncPath p : paths) {

			if (p.getFile() == null)
				continue;

			if (!p.getFile().exists())
				continue;

			// create the cache target filename based on the source path's hash
			// code
			Path target = mCachePath.resolve(Integer.toString(p.hashCode()));
			Path source = p.getPath();

			// update the cache table, adding new paths or updating the current
			// path's queuetime
			addOrUpdateCachedPath(p);

			if (p.getSyncType() == SyncType.SYNC_CREATE) {

				// create a path representing the final location on the target
				// filesystem
				File exTarget = getRootPath().resolve(p.getRelativePath())
						.toFile();

				// abort if the file exists on the target system, has the same
				// size and same time-date stamp.
				// TODO Replace with md5 checksum comparison
				if (exTarget.exists()) {

					if (exTarget.length() == source.toFile().length()
							&& exTarget.lastModified() == source.toFile()
									.lastModified())
						return;
				}

				// copies the source path saved in the syncpath object to the
				// cache target location (filename = sourcepath hashcode)
				copyFile(p.getPath(), target,
						StandardCopyOption.COPY_ATTRIBUTES);
			}

			// TODO need to determine what attributes are compared to decide if
			// file has been modified
			else {

				copyFile(p.getPath(), target,
						StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.COPY_ATTRIBUTES);
			}
		}
	}

	public String toString() {
		return mRootPath.toString();
	}

	protected void copyFile(final Path source, final Path target,
			final CopyOption... options) {

		Runnable r = () -> {
			try {
				Files.copy(source, target, options);
			} catch (IOException e) {
				e.printStackTrace();
			}
		};

		ThreadPool.getInstance().executeCachedTask(r);
	}

	protected void moveFile(final Path source, final Path target,
			final CopyOption... options) {
		Runnable r = () -> {
			try {
				Files.move(source, target, options);
			} catch (IOException e) {
				e.printStackTrace();
			}
		};

		ThreadPool.getInstance().executeCachedTask(r);
	}
}
