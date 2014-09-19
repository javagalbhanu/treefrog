package com.buddyware.treefrog.local.model;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;

import com.buddyware.treefrog.BaseTask;
import com.buddyware.treefrog.util.TaskMessage;
import com.buddyware.treefrog.util.TaskMessage.TaskMessageType;

public final class LocalWatchService extends BaseTask {

    private WatchService watcher;
    private LocalPathFinder finder;
    
    private final ExecutorService pathFinderExecutor = 
    									createExecutor ("pathFinder", false);
    
    private Integer finderDepth = Integer.MAX_VALUE;
    private final Map<WatchKey,Path> keys = new HashMap<WatchKey, Path>();
    
    private final ConcurrentLinkedQueue <Path> watchQueue = 
    										new ConcurrentLinkedQueue <Path> ();
    
    private final ObjectProperty <ArrayList <Path> > pathsFound = 
								new SimpleObjectProperty <ArrayList <Path>> ();

    private Boolean initialFind = true;
    private final Path rootPath;
    
	public LocalWatchService 
					(BlockingQueue <TaskMessage> messageQueue, Path rootPath) {

		super (messageQueue);
		this.rootPath = rootPath;
		
		//create the watch service
    	try {
			this.watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		setOnCancelled(new EventHandler() {

			@Override
			public void handle(Event arg0) {
				pathFinderExecutor.shutdown();
			}
		});    	
	};

	public void initializeWatchPaths() {
    	//start pathfinder for initial find.
		finderDepth = 2;
		ArrayList <Path> paths = new ArrayList <Path> ();
		paths.add (rootPath);
    	runPathFinder (paths);
    	finderDepth = Integer.MAX_VALUE;		
	}
	
	public ObjectProperty <ArrayList <Path> > pathsFound() {
		return this.pathsFound;
	}
	
	public void addPaths (ArrayList <Path> paths) {
		
		//execute path finder on an array list of paths
		runPathFinder (paths);
	}
	
	public final void addPath (Path dir) {
	
		//execute path finder on a single path
		ArrayList <Path> finderList = new ArrayList <Path> ();
		finderList.add (dir);

		runPathFinder (finderList);
	}
	
	private void runPathFinder (ArrayList <Path> paths) {
		
		//need to add blocking code / mechanism in case a path finder is 
		//currently running (rare case)

		finder = new LocalPathFinder (messageQueue, watchQueue);
		finder.setPaths (paths);
		finder.setDepth (finderDepth);
		
    	finder.setOnSucceeded(new EventHandler <WorkerStateEvent> () {

    		//whenever path finder is called save the paths to watch so a complete list
    		//is always available.
			@Override
			public void handle(WorkerStateEvent arg0) {
				pathsFound.set(finder.getPaths());
				
				//if this is the initial find, re-run the path finder
				//to finish recursing the specified paths.
				if (initialFind) {
					initialFind = false;
					runPathFinder (finder.getPaths());
				}
			}
    	});
    	
		pathFinderExecutor.execute (finder);    	
	}
	
    /**
     * Register the given directory with the WatchService
     * @throws InterruptedException 
     */
    public final void register(Path dir) 
    								throws IOException, InterruptedException {
 	
//System.out.println ("LocalWatchService.register() " + dir.toString());
    	//register the key with the watch service
        WatchKey key = 
    		dir.register (watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        
        if (!keys.isEmpty()) {
        	
            Path prev = keys.get(key);
            
            if (prev == null) {	                		
				//enqueueMessage ("Registered: " + dir, TaskMessageType.TASK_ACTIVITY);
            } 
            else {
	            if (!dir.equals(prev));
				//	enqueueMessage ("Registered: " + dir, TaskMessageType.TASK_ACTIVITY);
            }
        }

        keys.put(key, dir);
    }
		 
	private void processWatchEvent (WatchKey key, Path dir) {
		
    	for (WatchEvent<?> event: key.pollEvents()) {
	    	
	        WatchEvent.Kind kind = event.kind();
	
	        // TBD - provide example of how OVERFLOW event is handled
	        if (kind == OVERFLOW) {
	    		enqueueMessage ("Overflow encountered", TaskMessageType.TASK_ERROR);
	            continue;
	        }
	
	        // Context for directory entry event is the file name of entry
	        WatchEvent<Path> ev = cast(event);
	        String name = ev.context().toString();
	        Path child = dir.resolve(name);
	
	        if (kind == ENTRY_CREATE) {
	        	if (Files.isDirectory(child,  NOFOLLOW_LINKS)) {
	
						enqueueMessage ("Directory created: " + name, TaskMessageType.TASK_ACTIVITY);
	            		
	//        		if (recursive) {
							addPath (child);
	//        		}
	        	}
	            else {
	            		enqueueMessage ("File created: " + name, TaskMessageType.TASK_ACTIVITY);
	            }
	        }		                
	
	        if (kind == ENTRY_MODIFY)
	        		enqueueMessage ("File modified: " + name, TaskMessageType.TASK_ACTIVITY);
	        
	        if (kind == ENTRY_DELETE)
	        		enqueueMessage ("File deleted: " + name, TaskMessageType.TASK_ACTIVITY);
	    }
	}
    
    @SuppressWarnings("unchecked")
    <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
    
	@Override
	protected Void call () {

    boolean interrupted = false;
   
    initializeWatchPaths();
    
    try {
		// enter watch cycle
        while (!interrupted) {
System.out.println ("starting watch service");
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
               enqueueMessage ("Null directory key encountered.", TaskMessageType.TASK_ERROR);
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
		return null;
	};
}
