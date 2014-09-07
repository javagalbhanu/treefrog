package com.buddyware.treefrog.local.model;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import javafx.event.Event;
import javafx.event.EventHandler;

import com.buddyware.treefrog.BaseTask;
import com.buddyware.treefrog.util.TaskMessage;
import com.buddyware.treefrog.util.TaskMessage.TaskMessageType;

public final class LocalWatchService extends BaseTask {

    private WatchService watcher;
    private LocalPathFinder finder;
    
    private final Map<WatchKey,Path> keys = new HashMap<WatchKey, Path>();
    private final ConcurrentLinkedQueue <Path> watchQueue = new ConcurrentLinkedQueue <Path> ();
    private final ExecutorService pathFinderExecutor = createExecutor ("pathFinder", false);
    
	public LocalWatchService (BlockingQueue <TaskMessage> messageQueue) {

		super (messageQueue);
		
    	try {
			this.watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	/*
		setOnCancelled(new EventHandler() {

			@Override
			public void handle(Event arg0) {
				pathFinder.cancel();
			}
		}); */   	
	};

	public void addPaths (ArrayList <Path> paths) {
		
		//execute path finder on an array list of paths
		runPathFinder (paths);
	}
	
	public final ConcurrentLinkedQueue <Path> watchQueue() {
		return this.watchQueue;
	}
	
	public final void addPath (Path dir) {
		
		//execute path finder on a single path
		ArrayList <Path> finderList = new ArrayList <Path> ();
		finderList.add (dir);

		runPathFinder (finderList);
	}
	
	private void runPathFinder (ArrayList <Path> paths) {
		
		//need to add blocking code / mechanism in case a path finder is currently running (rare case)
		
		finder = new LocalPathFinder(messageQueue, this);
		pathFinderExecutor.execute(finder);
	}
	
    /**
     * Register the given directory with the WatchService
     * @throws InterruptedException 
     */
    public final void register(Path dir) throws IOException, InterruptedException {
 	
    	//register the key with the watch service
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        
        if (!keys.isEmpty()) {
        	
            Path prev = keys.get(key);
            
            if (prev == null) {	                		
				messageQueue.put(new TaskMessage ("Registered: " + dir, TaskMessageType.TASK_ACTIVITY));
            } 
            else {
	            if (!dir.equals(prev));
					messageQueue.put(new TaskMessage ("Registered: " + dir, TaskMessageType.TASK_ACTIVITY));
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
System.out.println ("Starting watch service");    
    try {
		// enter watch cycle
        while (!interrupted) {
			
			 //watch for a key change.  Thread blocks until a change occurs
	    	WatchKey key = null;
	    	interrupted = isCancelled();
	    	
	     //    try {

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
	        /*	 
	         } catch (InterruptedException x) {
	         	x.printStackTrace();
	             return null;
	         }*/
			
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
System.out.println ("exiting watch service...");
		return null;
	};
}
