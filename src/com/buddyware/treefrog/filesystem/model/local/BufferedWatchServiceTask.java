package com.buddyware.treefrog.filesystem.model.local;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import com.buddyware.treefrog.BaseTask;
import com.buddyware.treefrog.ThreadPool;
import com.buddyware.treefrog.filesystem.model.SyncBuffer;
import com.buddyware.treefrog.filesystem.model.SyncPath;
import com.buddyware.treefrog.filesystem.model.SyncType;
import com.buddyware.treefrog.util.utils;

public final class BufferedWatchServiceTask extends BaseTask <Void> {

	private final static String TAG  = "\n\tLocalWatchService";
	private final static int BUFFER_INTERVAL = 1000;
	
	private final SyncBuffer mBuffer;

	//watch service task
    private WatchService watcher;
    
    //path finding task reference
    private Future <List <Path>> mFinder;
    
    //root path where the watch service begins 
    private final Path mRootPath;

    //class hash map which keys watched paths to generated watch keys
    private final Map<WatchKey, Path> keys = new HashMap<WatchKey, Path>();
    
    //reference to model property of watched paths.
    private final SimpleListProperty <SyncPath> mChangedPaths = 
    		new SimpleListProperty <SyncPath> 
    							(FXCollections.<SyncPath> observableArrayList());
 
	public BufferedWatchServiceTask (String rootPath) {

		super ();
	
		mRootPath = Paths.get(rootPath);
		mBuffer = new SyncBuffer (mRootPath);
		
		//create the watch service
    	try {
			this.watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
		setOnCancelled(arg0 -> {
			
			if (mFinder == null)
				return;

			mFinder.cancel(true);
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
		if (mFinder != null) {
			if (!mFinder.isDone()) {
				System.err.println (TAG + ".runPathFinder(): " +
						"\n\tFinder task already running.  Unable to find paths!");
				return;
			}
		}

		final LocalPathFinderTask finder = new LocalPathFinderTask (paths);
				
		//callbacks on successful completion of pathfinder

		EventHandler <WorkerStateEvent> eh = 
			new EventHandler <WorkerStateEvent> () {

				ArrayList <SyncPath> paths = new ArrayList <SyncPath>();
				
				@Override
				public void handle(WorkerStateEvent arg0) {
						try {
							for (Path p: finder.get()) {
								
								SyncPath sp = new SyncPath(mRootPath, p, SyncType.SYNC_NONE);
								sp.setQueuedTime(System.currentTimeMillis());
								
								paths.add(sp);
							}
						} catch (InterruptedException | ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
					addPaths(paths);
				}
				
			};
			
    	finder.setOnSucceeded(eh);
		mFinder = ThreadPool.getInstance().executeCachedTask (finder);
	
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
System.out.println(TAG + ".processWatchEvent() detected event for " + dir);	    	
            Kind<?> kind = event.kind();		
            
	        // TBD - provide example of how OVERFLOW event is handled
	        if (kind == OVERFLOW) {
	    		System.out.println ("Overflow encountered");
	        }
	        
	        WatchEvent<Path> ev = (WatchEvent<Path>) event;
	        Path target = dir.resolve(ev.context());

			//if this is a push from another filesystem, store the path
			//of the cached file for observation and later file moving
			//or update the current entry to indicate it has been changed again

			ArrayList <Path> finderList = new ArrayList <Path> ();
			
			if (Files.isDirectory(target)) {
				
				if (kind == ENTRY_CREATE) {

					finderList.add (target);
					runPathFinder (finderList);
				}
			}
			else {
				if (kind == ENTRY_DELETE) {
System.out.println(TAG + ".processWatchPaths DELETE\n\t" + target.toString());					
					//if the file being deleted has been queued for sync, 
					//remove it from the queue.
					mBuffer.deleteIfExists(target); 
;						
					addPath (target, SyncType.SYNC_DELETE);
					
				} else if (kind == ENTRY_CREATE) {
System.out.println(TAG + ".processWatchPaths CREATE\n\t" + target.toString());
	    			if (Files.isReadable(target))	    				
						mBuffer.addOrUpdatePath(target, SyncType.SYNC_CREATE);
	    			else
	    				System.err.println ("File " + target + " cannot be read");
	    			
				} else if (kind == ENTRY_MODIFY) {
System.out.println(TAG + ".processWatchPaths MODIFY\n\t" + target.toString());					
					mBuffer.addOrUpdatePath(target,  SyncType.SYNC_MODIFY);
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

    try {
    initializeWatchPaths();
    } catch (Exception e) {
    	e.printStackTrace();
    }
    try {
		// enter watch cycle
        while (!interrupted) {
System.out.println(TAG + ".call() " + mRootPath);  
            
			 //watch for a key change.  Thread blocks until a change occurs
	    	WatchKey key = null;
	    	interrupted = isCancelled();

        	 //thread blocks until a key change occurs 
        	 // (whether a new path is processed by finder or a watched item changes otherwise)

            try {

            	if (mBuffer.isEmpty()) {
System.out.println(TAG + ".call() take\n\t" + mRootPath);              		
            		key = watcher.take();
            	}
            	else {
System.out.println(TAG + ".call() poll\n\t" + mRootPath);            		
            		addPaths (mBuffer.getExpiredPaths (BUFFER_INTERVAL));            		
            		key = watcher.poll(BUFFER_INTERVAL, TimeUnit.MILLISECONDS);
            	}
            	
            } catch (InterruptedException e) {
                interrupted = true;
                try {
					watcher.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                e.printStackTrace();
                // fall through and retry
            }
                        

            //if the key is null and no watchpaths exist, quit
            //otherwise, continue loop
            if (key == null) {
            	if (keys.isEmpty())
            		break;
            	continue;
            }
            		
            //process key change once it occurs
            Path dir = keys.get (key);
System.out.println(".call() processing watch events") ;           
            if (dir != null)
                processWatchEvent(key, dir);

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
}