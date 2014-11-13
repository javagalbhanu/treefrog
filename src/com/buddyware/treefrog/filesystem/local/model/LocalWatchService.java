package com.buddyware.treefrog.filesystem.local.model;

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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
      
    //reference to model property of watched paths.
    private final SimpleListProperty <String> mAddedPaths = 
    		new SimpleListProperty <String> 
    							(FXCollections.<String> observableArrayList());
    
    private final SimpleListProperty <String> mRemovedPaths = 
    		new SimpleListProperty <String> 
    							(FXCollections.<String> observableArrayList());
    
    private final SimpleListProperty <String> mChangedPaths = 
    		new SimpleListProperty <String> 
    							(FXCollections.<String> observableArrayList());    
 
	public LocalWatchService () {

		super ();
	
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
		
		mAddedPaths.addListener(new ListChangeListener <String> (){

			@Override
			public void onChanged( 
				javafx.collections.ListChangeListener.Change<? extends String> 
								arg0) {
				
					for (String pathName: arg0.getList()) {
						try {
							register (Paths.get(pathName));
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
				}
			});
	};

	public SimpleListProperty<String> addedPaths() {
		return mAddedPaths;
	}
	
	public SimpleListProperty<String> removedPaths() {
		return mRemovedPaths;
	}

	public SimpleListProperty<String> changedPaths() {
		return mChangedPaths;
	}
	
	public void initializeWatchPaths() {
		
		ArrayList <String> paths = new ArrayList <String> ();
		
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
			Files.newDirectoryStream (LocalWatchPath.getRootPath(), filter)) {
			
			for (Path entry: stream)
				paths.add(entry.toString());
			
		} catch (IOException x) {
			x.printStackTrace();
		}
		
		runPathFinder (paths);
	}
	
	public final void addPath (Path dir) {

		ArrayList <String> finderList = new ArrayList <String> ();
		
		finderList.add (dir.toString());

		runPathFinder (finderList);
	}
	
	public final void removePath (String dir) {
		/*
		 * Paths (and subpaths) the watch service reports as having been deleted
		 * need to be pushed to the removedPaths property for model-level
		 * notification
		 * 
		 * Watch service automatically deletes invalidated keys from local
		 * hashmap
		 */
		
		mRemovedPaths.setAll(dir);
	}
	
	public final void removeDeletedPath (String pathName) {

	}
	
	private void runPathFinder (ArrayList <String> paths) {
		
		//need to add blocking code / mechanism in case a path finder is 
		//currently running (rare case)
		
		finder = new LocalPathFinder();
		finder.setPaths (paths);
		
		//callbacks on successful completion of pathfinder

		EventHandler <WorkerStateEvent> eh = 
			new EventHandler <WorkerStateEvent> () {

				@Override
				public void handle(WorkerStateEvent arg0) {
						mAddedPaths.setAll(finder.getPathNames());
					}
			};
			
    	finder.setOnSucceeded(eh);
 	
		pathFinderExecutor.execute (finder);    	
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
				//enqueueMessage ("Registered: " + dir, TaskMessageType.TASK_ACTIVITY);
            } 
            else {
	            if (!dir.equals(prev));
				//	enqueueMessage ("Registered: " + dir, TaskMessageType.TASK_ACTIVITY);
            }
        }

        keys.put(key, dir);
    }
		 
	private void processWatchEvent (WatchKey key, Path dir) throws IOException, InterruptedException {
		
    	for (WatchEvent<?> event: key.pollEvents()) {
	    	
            WatchEvent.Kind kind = event.kind();		
            
            System.out.println("Kind: " + event.kind());
            System.out.println("Context: " + event.context());
            System.out.println("Count: " + event.count());            
            System.out.println();   
            
	        // TBD - provide example of how OVERFLOW event is handled
	        if (kind == OVERFLOW) {
	    		System.out.println ("Overflow encountered");
	        }
	        
	        WatchEvent<Path> ev = (WatchEvent<Path>)event;
	        Path target = dir.resolve(ev.context());
	        
	        if (kind == ENTRY_DELETE) {
	        	System.out.println ("File deleted: " + dir.resolve(target).toString());
	        	removePath (target.toString());	        	
	        
	        } else if (kind == ENTRY_CREATE) {
	        	System.out.println ("File added: " + dir.resolve(target).toString());
	        	register (target);
	        	addPath (target);
	        	

	        } else if (kind == ENTRY_MODIFY) {
	        	System.out.println ("File modified: " + dir.toString());
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
    
    initializeWatchPaths();

    //register (LocalWatchPath.getRootPath());
    
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
		return null;
	};
}
