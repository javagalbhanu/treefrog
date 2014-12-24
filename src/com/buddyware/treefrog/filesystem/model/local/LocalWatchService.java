package com.buddyware.treefrog.filesystem.model.local;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;

import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;
import java.util.Map;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;

import com.buddyware.treefrog.BaseTask;
import com.buddyware.treefrog.filesystem.model.SyncBuffer;
import com.buddyware.treefrog.filesystem.model.SyncPath;
import com.buddyware.treefrog.filesystem.model.SyncType;
import com.buddyware.treefrog.util.utils;

public final class LocalWatchService extends BaseTask {

	private final static String TAG  = "\n\tLocalWatchService";
	private final static int BUFFER_INTERVAL = 1000;
	
	private final SyncBuffer mCacheBuffer;
	private final SyncBuffer mPublishBuffer;
	
	//watch service task
    private WatchService watcher;
    
    //path finding task and associated executor
    private LocalPathFinder finder;
    
    //root path where the watch service begins 
    private final Path mRootPath;
    private final Path mCachePath;
    
    private final ExecutorService pathFinderExecutor = 
    									createExecutor ("pathFinder", false);

    //class hash map which keys watched paths to generated watch keys
    private final Map<WatchKey, Path> keys = new HashMap<WatchKey, Path>();
    
    //reference to model property of watched paths.
    private final SimpleListProperty <SyncPath> mChangedPaths = 
    		new SimpleListProperty <SyncPath> 
    							(FXCollections.<SyncPath> observableArrayList());
 
	public LocalWatchService (String rootPath) {

		super ();
	
		mRootPath = Paths.get(rootPath);
		mCachePath = mRootPath.resolve(".cache");
		
		mCacheBuffer = new SyncBuffer (mRootPath);
		mPublishBuffer = new SyncBuffer (mRootPath);
		
		//create the watch service
    	try {
			this.watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
		setOnCancelled(new EventHandler() {

			@Override
			public void handle(Event arg0) {
				pathFinderExecutor.shutdown();
			}
		});
		
		mChangedPaths.addListener(new ListChangeListener <SyncPath> (){

			@Override
			public final synchronized void onChanged( 
				javafx.collections.ListChangeListener.Change<? extends SyncPath> 
								arg0) {
				
				if (arg0.getList().isEmpty())
					return;
			
					for (SyncPath path: arg0.getList()) {

						//call register only when a directory is found
						if (path.getFile() == null) {
							try {
								register (path.getPath());
							} catch (IOException e) {
								e.printStackTrace();
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
						}
					}
				}
			});
	};

	public final synchronized SimpleListProperty<SyncPath> changedPaths() { return mChangedPaths; }
	
	public final synchronized  void initializeWatchPaths() {
		
		ArrayList <Path> paths = new ArrayList <Path> ();
		
		//create a DirectoryStream filter that finds only directories
		//and symlinks
		
		DirectoryStream.Filter<Path> filter = 
			new DirectoryStream.Filter<Path>() {
			
				public boolean accept(Path file) throws IOException {
					
					return (Files.isDirectory(file) || 
							Files.isSymbolicLink(file));
				}
			};
		
		//apply the filter to a directory stream opened on the root path
		//and save everything returned.
		paths.addAll(utils.getFiles(mRootPath, filter));
		
		runPathFinder (paths);
	}

	private final synchronized void runPathFinder (ArrayList <Path> paths) {
		
		//need to add blocking code / mechanism in case a path finder is 
		//currently running (rare case)
		
		finder = new LocalPathFinder();
		finder.setPaths (paths);
		
		//callbacks on successful completion of pathfinder

		EventHandler <WorkerStateEvent> eh = 
			new EventHandler <WorkerStateEvent> () {

				ArrayList <SyncPath> paths = new ArrayList <SyncPath>();
				
				@Override
				public void handle(WorkerStateEvent arg0) {
						for (Path p: finder.getPaths()) {
							
							SyncPath sp = new SyncPath(mRootPath, p, SyncType.SYNC_NONE);
							sp.setQueuedTime(System.currentTimeMillis());
							
							paths.add(sp);
						}
					
					addPaths(paths);
				}
				
			};
			
    	finder.setOnSucceeded(eh);
 	
		pathFinderExecutor.execute (finder);    	
	}
	
	public final synchronized void cachePath(SyncPath path) {
		
		if (path == null)
			return;
		
		mCacheBuffer.addOrUpdatePath(path);
	}
	
	private final synchronized void addPath(Path path, SyncType syncType) {
		
		if (path == null)
			return;
		
		SyncPath p = new SyncPath (mRootPath, path, syncType);
		p.setQueuedTime(System.currentTimeMillis());
		
		mChangedPaths.setAll(p);
	}
	
	private final synchronized void addPaths(List<SyncPath> paths) {
		
		if (paths == null)
			return;
	
		mChangedPaths.setAll(paths);
	}

    /**
     * Register the given directory with the WatchService
     * @throws InterruptedException 
     */
    public final synchronized void register(Path dir) 
    								throws IOException, InterruptedException {
    	
    	//register the key with the watch service
        WatchKey key = 
    		dir.register (watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        
        if (!keys.isEmpty()) {
        
        	Path prev = keys.get(key);

        	if (prev == null) {
        		//This is a new key
        	}
        	else if (!dir.equals(prev)) {
        		//This is an update
        	}
        }
        
        keys.put(key, dir);
    }
    
	private final synchronized void processWatchEvent (WatchKey key, Path dir) throws IOException, InterruptedException {

    	for (WatchEvent<?> event: key.pollEvents()) {
	    	
            WatchEvent.Kind kind = event.kind();		
            
	        // TBD - provide example of how OVERFLOW event is handled
	        if (kind == OVERFLOW) {
	    		System.out.println ("Overflow encountered");
	        }
	        
	        WatchEvent<Path> ev = (WatchEvent<Path>)event;
	        Path target = dir.resolve(ev.context());

			//if this is a push from another filesystem, store the path
			//of the cached file for observation and later file moving
			//or update the current entry to indicate it has been changed again

			if (dir.equals(mCachePath)) { 

				if (kind == ENTRY_DELETE)
					mCacheBuffer.deleteIfExists(Integer.parseInt(ev.context().toString()));
				else
					mCacheBuffer.updatePath(Integer.parseInt(ev.context().toString()));

				continue;
			}
			
			ArrayList <Path> finderList = new ArrayList <Path> ();
			
			if (Files.isDirectory(target)) {
				
				if (kind == ENTRY_CREATE) {

					finderList.add (target);
					runPathFinder (finderList);
				}
			}
			else {
				if (kind == ENTRY_DELETE) {
					
					//if the file being deleted has been queued for sync, 
					//remove it from the queue.
					mPublishBuffer.deleteIfExists(target); 
;						
					addPath (target, SyncType.SYNC_DELETE);
					
				} else if (kind == ENTRY_CREATE) {

	    			if (Files.isReadable(target)) {
	    				
	    				//update queued time if it's a cached path
						mPublishBuffer.addOrUpdatePath(target, SyncType.SYNC_CREATE);

	    			}
	    			else
	    				System.err.println ("File " + target + " cannot be read");
	    			
				} else if (kind == ENTRY_MODIFY) {				
					mPublishBuffer.addOrUpdatePath(target,  SyncType.SYNC_MODIFY);
				}
			}

	        boolean valid = key.reset();
	        
	        if (!valid)
	        	break;
	    }
	}
	
    @SuppressWarnings("unchecked")
    <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
    
	@Override
	protected Void call () throws IOException, InterruptedException {

    boolean interrupted = false;
    
    register (mRootPath);
    initializeWatchPaths();
    
    try {
		// enter watch cycle
        while (!interrupted) {

            // all directories are inaccessible
            if (keys.isEmpty())
                break;
            
			 //watch for a key change.  Thread blocks until a change occurs
	    	WatchKey key = null;
	    	interrupted = isCancelled();

        	 //thread blocks until a key change occurs 
        	 // (whether a new path is processed by finder or a watched item changes otherwise)

            try {

            	if (mPublishBuffer.isEmpty() && mCacheBuffer.isEmpty())
            		key = watcher.take();
            	else
            		key = watcher.poll(BUFFER_INTERVAL, TimeUnit.MILLISECONDS);

            } catch (InterruptedException e) {
                interrupted = true;
                try {
					watcher.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                // fall through and retry
            }
                        
            
            //process key change once it occurs
            
            Path dir = keys.get (key);
            
            if (dir != null)
                processWatchEvent(key, dir);

            processOutboundPaths();
            processCachedPaths();

			if (key == null)
				continue;
			
            // reset key and remove from set if directory no longer accessible
            if (!key.reset())
            	keys.remove(key);

        }
	} finally {
        if (interrupted)
            Thread.currentThread().interrupt();
    }
    	keys.clear();
    	watcher.close();
    	
		return null;
	};
	
	private final synchronized void processOutboundPaths() {

		if (mPublishBuffer.isEmpty())
			return;
		
		addPaths (mPublishBuffer.getExpiredPaths (BUFFER_INTERVAL));

	}
	
	private final synchronized void processCachedPaths() {
		
		if (mCacheBuffer.isEmpty())
			return;
		
		List <SyncPath> paths = mCacheBuffer.getExpiredPaths (BUFFER_INTERVAL);
		
		if (paths == null)
			return;
					
		if (paths.isEmpty())
			return;
		
		for (SyncPath p:paths) {
			
			File f = p.getFile();
			
			if (f == null)
				continue;
			
			try {
				Files.move(	mCachePath.resolve (Integer.toString(p.hashCode())), 
							mRootPath.resolve(p.getRelativePath()),
							StandardCopyOption.REPLACE_EXISTING, 
							StandardCopyOption.ATOMIC_MOVE);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
