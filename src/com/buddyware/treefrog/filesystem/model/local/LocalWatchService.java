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
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;

import com.buddyware.treefrog.BaseTask;
import com.buddyware.treefrog.filesystem.model.SyncPath;
import com.buddyware.treefrog.filesystem.model.SyncType;
import com.buddyware.treefrog.util.utils;

public final class LocalWatchService extends BaseTask {

	private final static String TAG  = "LocalWatchService";
	//watch service task
    private WatchService watcher;
    
    //path finding task and associated executor
    private LocalPathFinder finder;
    
    //root path where the watch service begins 
    private final Path mRootPath;
    
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
			public void onChanged( 
				javafx.collections.ListChangeListener.Change<? extends SyncPath> 
								arg0) {
System.out.println(TAG + ": " + arg0.getList().size() + " paths added to queue");				
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

	public SimpleListProperty<SyncPath> changedPaths() { return mChangedPaths; }
	
	public void initializeWatchPaths() {
		
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
	
	private void runPathFinder (ArrayList <Path> paths) {
		
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
							paths.add(
								new SyncPath(mRootPath, p, SyncType.SYNC_NONE));
						}
					
					addPaths(paths);
				}
				
			};
			
    	finder.setOnSucceeded(eh);
 	
		pathFinderExecutor.execute (finder);    	
	}
	
	private void addPath(Path path, SyncType syncType) {
		mChangedPaths.setAll(new SyncPath(mRootPath, path, syncType));
	}
	
	private void addPaths(ArrayList<SyncPath> paths) {
		mChangedPaths.setAll(paths);
	}
	
    /**
     * Register the given directory with the WatchService
     * @throws InterruptedException 
     */
    public final void register(Path dir) 
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
        
    	//System.out.println(TAG + ".register(): Registering " + dir.toString() +" with key " + key.toString());
        
        keys.put(key, dir);
    }
    
	private void processWatchEvent (WatchKey key, Path dir) throws IOException, InterruptedException {
		
    	for (WatchEvent<?> event: key.pollEvents()) {
	    	
            WatchEvent.Kind kind = event.kind();		
            
	        // TBD - provide example of how OVERFLOW event is handled
	        if (kind == OVERFLOW) {
	    		System.out.println ("Overflow encountered");
	        }
	        
	        WatchEvent<Path> ev = (WatchEvent<Path>)event;
	        Path target = dir.resolve(ev.context());
	        
	        if (kind == ENTRY_DELETE) {

	        	ArrayList <Path> finderList = new ArrayList <Path> ();
	        	
	        	if (Files.isDirectory(target)) {
	        		//directory deletion is not implemented apart from
	        		//file deletion
	        	}
	        	else
	        		addPath (target, SyncType.SYNC_DELETE);
	        
	        } else if (kind == ENTRY_CREATE) {
	        	
	        	/*
	        	 * Added paths are passed to the pathfinder service for
	        	 * subdirectory discovery.  Path and subpaths are then added
	        	 * to the AddedPaths property via an event listener on
	        	 * service's onSucceeded() event.
	        	 * 
	        	 * Added files are added directly to the AddedPaths property
	        	 */
	        	
	    		ArrayList <Path> finderList = new ArrayList <Path> ();
   	    		
	    		if (Files.isDirectory(target)) {
	    			finderList.add (target);
	    			runPathFinder (finderList);
	    		}
	    		//add files directly to the addedPaths property
	    		else {
    			
	    			//a newly created file may not be immediately readable
	    			if (Files.isReadable(target)) {
	    					addPath (target, SyncType.SYNC_CREATE);
	    			}
	    			else
	    				System.err.println ("File " + target + " cannot be read");
	    		}

	        } else if (kind == ENTRY_MODIFY) {
	        	System.out.println ("File modified: " + target.toString());
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

			 //watch for a key change.  Thread blocks until a change occurs
	    	WatchKey key = null;
	    	interrupted = isCancelled();

        	 //thread blocks until a key change occurs 
        	 // (whether a new path is processed by finder or a watched item changes otherwise)

            try {
                key = watcher.take();
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
		
            Path dir = keys.get (key);
            
            if (dir == null) {
               System.out.println ("Null directory key encountered.");
                continue;
            }

            //process key change once it occurs
            processWatchEvent(key, dir);
           
            // reset key and remove from set if directory no longer accessible
            if (!key.reset()) {

            	keys.remove(key);
 
                // all directories are inaccessible
                if (keys.isEmpty())
                    break;
            }
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
