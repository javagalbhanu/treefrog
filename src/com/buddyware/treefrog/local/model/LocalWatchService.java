package com.buddyware.treefrog.local.model;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayDeque;
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

	//watch service task
    private WatchService watcher;
    
    //path finding task and associated executor
    private LocalPathFinder finder;
    
    private final ExecutorService pathFinderExecutor = 
    									createExecutor ("pathFinder", false);

    //class hash map which keys watched paths to generated watch keys
    private final Map<WatchKey, Path> keys = new HashMap<WatchKey, Path>();
    
    //queue shared between watchservice and pathfinder sub tasks
    private final ConcurrentLinkedQueue <Path> watchQueue = 
								new ConcurrentLinkedQueue <Path> ();
    
    //returns paths added to the watch service
    private final ObjectProperty <ArrayDeque <LocalWatchPath> > addedPaths = 
					new SimpleObjectProperty <ArrayDeque <LocalWatchPath> > ();

    //returns paths removed from the watch service
    private final ObjectProperty <ArrayDeque <LocalWatchPath> > removedPaths = 
			new SimpleObjectProperty <ArrayDeque <LocalWatchPath> > ();
    
    
	public LocalWatchService (BlockingQueue <TaskMessage> messageQueue) {

		super (messageQueue);
		
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

	};


	public void initializeWatchPaths() {
		
		ArrayDeque <LocalWatchPath> paths = new ArrayDeque <LocalWatchPath> ();
		
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
		try (DirectoryStream <Path> stream = 
			Files.newDirectoryStream (LocalWatchPath.getRoot(), filter)) {
			
			for (Path entry: stream)
				paths.add(new LocalWatchPath (entry));
			
		} catch (IOException x) {
			x.printStackTrace();
		}
		
		//save the list to the watch service property for UI notification
System.out.println ("LocalWatchService: retrivied inital list.  Setting property...");		
		this.addedPaths.setValue(paths);

System.out.println ("LocalWatchService: adding inital list for recursion...");		
		addPaths (paths);
	}
	
	public ObjectProperty <ArrayDeque <LocalWatchPath> > addedPaths() {
		return this.addedPaths;
	}

	public ObjectProperty <ArrayDeque <LocalWatchPath> > removedPaths() {
		return this.removedPaths;
	}
	
	public void addPaths (ArrayDeque <LocalWatchPath> paths) {
		
		//execute path finder on an array list of paths
		runPathFinder (paths, false);
	}
	
	public final void addPath (Path dir) {

		ArrayDeque <LocalWatchPath> finderList = 
											new ArrayDeque <LocalWatchPath> ();
		finderList.add (new LocalWatchPath (dir));

		runPathFinder (finderList, false);
	}
	
	public final void removePath (Path dir) {
		
		ArrayDeque <LocalWatchPath> finderList = 
											new ArrayDeque <LocalWatchPath> ();
		finderList.add(new LocalWatchPath (dir));
		
		runPathFinder (finderList, true);
	}
	
	private void runPathFinder (ArrayDeque <LocalWatchPath> paths, 
														boolean isRemoving) {
		
		//need to add blocking code / mechanism in case a path finder is 
		//currently running (rare case)

		finder = new LocalPathFinder (messageQueue, watchQueue);
		finder.setPaths (paths);
		
		//callbacks on successful completion of pathfinder

		EventHandler eh = null;
		
		if (!isRemoving) {
			eh = new EventHandler <WorkerStateEvent> () {
	
				@Override
				public void handle(WorkerStateEvent arg0) {
					addedPaths.set(finder.getPaths());
				}
			};
			
		} else {
			eh = new EventHandler <WorkerStateEvent> () {
				
				@Override
				public void handle(WorkerStateEvent arg0) {
					removedPaths.set(finder.getPaths());
				}
			};			
		}
		
    	finder.setOnSucceeded(eh);
    	
		pathFinderExecutor.execute (finder);    	
	}
	
    /**
     * Register the given directory with the WatchService
     * @throws InterruptedException 
     */
    public final void register(Path dir) 
    								throws IOException, InterruptedException {
 	
System.out.println ("LocalWatchService.register() " + dir.toString());
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

	        if (kind == ENTRY_DELETE) {
	        	//removePath (target);
	        	enqueueMessage ("File deleted: " + dir.toString(), TaskMessageType.TASK_ACTIVITY);
	        	continue;
	        }
	        
	        if (kind == ENTRY_CREATE) {
	        	enqueueMessage ("File added: " + dir.toString(), TaskMessageType.TASK_ACTIVITY);
	        	addPath (dir);
	        	continue;
	        }
	        
	        if (kind == ENTRY_MODIFY) {
	        	enqueueMessage ("File modified: " + dir.toString(), TaskMessageType.TASK_ACTIVITY);
	        }
	    }
	}
	
    @SuppressWarnings("unchecked")
    <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
    
	@Override
	protected Void call () throws IOException, InterruptedException {

    boolean interrupted = false;

    register (LocalWatchPath.getRoot());

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