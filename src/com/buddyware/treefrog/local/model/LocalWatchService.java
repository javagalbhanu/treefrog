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

    private WatchService watcher;
    private LocalPathFinder finder;
    
    private final ExecutorService pathFinderExecutor = 
    									createExecutor ("pathFinder", false);

    private final Map<WatchKey,Path> keys = new HashMap<WatchKey, Path>();
    
    private final ConcurrentLinkedQueue <Path> watchQueue = 
    										new ConcurrentLinkedQueue <Path> ();
    
    private final ObjectProperty <ArrayDeque <Path> > pathsFound = 
								new SimpleObjectProperty <ArrayDeque <Path> > ();

    private final Path rootPath;
    
	public LocalWatchService 
					(BlockingQueue <TaskMessage> messageQueue, Path rootPath) {

		super (messageQueue);
		this.rootPath = rootPath;
		
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
		
		ArrayDeque <Path> paths = new ArrayDeque <Path> ();
		
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
			Files.newDirectoryStream (rootPath, filter)) {
			
			for (Path entry: stream) {
				
				Path xpath = entry;

				if (Files.isSymbolicLink(entry))
					xpath = Files.readSymbolicLink(entry);
				
				paths.add(xpath);
			}
			
		} catch (IOException x) {
			x.printStackTrace();
		}
		
		//save the list to the watch service property for UI notification
System.out.println ("LocalWatchService: retrivied inital list.  Setting property...");		
		this.pathsFound.setValue(paths);

System.out.println ("LocalWatchService: adding inital list for recursion...");		
		addPaths (paths);
	}
	
	public ObjectProperty <ArrayDeque <Path> > pathsFound() {
		return this.pathsFound;
	}
	
	public void addPaths (ArrayDeque <Path> paths) {
		
		//execute path finder on an array list of paths
		runPathFinder (paths);
	}
	
	public final void addPath (Path dir) {
	
		//execute path finder on a single path
		ArrayDeque <Path> finderList = new ArrayDeque <Path> ();
		finderList.add(dir);

		runPathFinder (finderList);
	}
	
	public final void removePath (Path dir) {
		System.out.println ("removePath stub code goes here!!!");
	}
	
	private void runPathFinder (ArrayDeque <Path> paths) {
		
		//need to add blocking code / mechanism in case a path finder is 
		//currently running (rare case)

		finder = new LocalPathFinder (messageQueue, watchQueue);
		finder.setPaths (paths);
		
    	finder.setOnSucceeded(new EventHandler <WorkerStateEvent> () {

			@Override
			public void handle(WorkerStateEvent arg0) {
				pathsFound.set(finder.getPaths());
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

            Path target = resolveTarget (event, dir);
            
	        // TBD - provide example of how OVERFLOW event is handled
	        if (kind == OVERFLOW) {
	    		enqueueMessage ("Overflow encountered", TaskMessageType.TASK_ERROR);
	            continue;
	        }
	
	        if (kind == ENTRY_CREATE)
	        	addPath (target);
	
	        if (kind == ENTRY_MODIFY)
	        	enqueueMessage ("File modified: " + target.toString(), TaskMessageType.TASK_ACTIVITY);
	        
	        if (kind == ENTRY_DELETE) {
	        	removePath (target);
	        	enqueueMessage ("File deleted: " + target.toString(), TaskMessageType.TASK_ACTIVITY);
	        }
	    }
	}
    
	private Path resolveTarget (WatchEvent <?> event, Path dir) {
		
        WatchEvent<Path> ev = cast(event);
        dir.resolve(ev.context().toString());
        
    	if (Files.isDirectory(dir,  NOFOLLOW_LINKS))
			return dir;
    	
    	if (Files.isSymbolicLink(dir))
			try {
				return dir.toRealPath();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
    	
    	return dir;        
	}
	
    @SuppressWarnings("unchecked")
    <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
    
	@Override
	protected Void call () throws IOException, InterruptedException {

    boolean interrupted = false;

    register (rootPath);

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
