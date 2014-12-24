package com.buddyware.treefrog.filesystem.model;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SyncBuffer {

	private final Map <Integer, SyncPath> mBuffer = 
											new HashMap <Integer, SyncPath> ();
	
	private final static String TAG = "\nSyncbuffer";
	
	private final Path mRootPath;
	
	public SyncBuffer(Path rootPath) {
		mRootPath = rootPath;
	}
	
	public synchronized boolean updatePath (Integer hash) {
		
		SyncPath p = mBuffer.get(hash);
		
		boolean success = (p != null);
		
		if (success)	
			p.setQueuedTime (System.currentTimeMillis());
		
		return success;
	}
	
	public synchronized boolean updatePath (Path path) {
    	return updatePath(path.hashCode()); 
	}
	
	public synchronized boolean isEmpty() { return mBuffer.isEmpty(); }
	
	public synchronized boolean deleteIfExists (Integer hash) {
	
		boolean success = mBuffer.containsKey(hash);
		
		if (success)
			mBuffer.remove(hash);
		
		return success;
		
	}
	
	public synchronized boolean deleteIfExists (Path path) {
	
		return deleteIfExists (path.hashCode());
	}
	
	public synchronized void addOrUpdatePath (SyncPath path) {
		
		//saves an existing SyncPath under it's hashcode
		if (updatePath (path.getPath()))
			return;
		
		path.setQueuedTime(path.getQueuedTime());
	
		mBuffer.put(path.hashCode(), path);
	}
	
	public synchronized void addOrUpdatePath (Path path, SyncType sync_type) {
		
		//creates a new syncpath if it doesn't already exist
		if (updatePath (path))
			return;

		//otherwise, add a new syncpath
		SyncPath p = new SyncPath(mRootPath, path, sync_type);
		p.setQueuedTime(System.currentTimeMillis());
	
		mBuffer.put(p.hashCode(), p);
	}
	
	public synchronized List <SyncPath> getExpiredPaths(long interval) {
		
		if (mBuffer.isEmpty())
			return null;

		List <SyncPath> paths = null;
		
		Iterator it = mBuffer.entrySet().iterator();
		
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			SyncPath p = (SyncPath) pair.getValue();
			
		
			if ( (p.getQueuedTime() + interval) > System.currentTimeMillis())
				continue;
			
			if (paths == null)
				paths = new ArrayList <SyncPath> ();
			
			paths.add(p);
			it.remove();
		}
		
		return paths;
	}
}
