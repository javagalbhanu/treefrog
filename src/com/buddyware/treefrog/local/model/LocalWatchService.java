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
import java.util.Map;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class LocalWatchService extends Service<Void> {

    private WatchService watcher;
    
    private Map<WatchKey,Path> keys;
    
    private boolean recursive = true;
    private boolean trace = false;
    
    public LocalWatchService() {

    	try {
			this.watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    }
    
	public Map<WatchKey,Path> getKeys() {
		return keys;
	}
		 
    @Override
    protected Task<Void> createTask() {
    	
        return new Task<Void>() {
        	
            @Override
            protected Void call() throws Exception {
            	System.out.println ("Updating message property from local watch service task...");
            	updateMessage("Began local watch service task...");
            	
		        for (;;) {
		   		 
		            // wait for key to be signaled
		            WatchKey key;
		            
		            try {
		                key = watcher.take();
		            } catch (InterruptedException x) {
		            	x.printStackTrace();
		                return null;
		            }
		 
		            Path dir = keys.get(key);
		            
		            if (dir == null) {
		                System.err.println("unrecognized watchkey");
		                continue;
		            }
		 
		            for (WatchEvent<?> event: key.pollEvents()) {
		            	
		                WatchEvent.Kind kind = event.kind();
		 
		                // TBD - provide example of how OVERFLOW event is handled
		                if (kind == OVERFLOW) {
		                    continue;
		                }
		 
		                // Context for directory entry event is the file name of entry
		                WatchEvent<Path> ev = cast(event);
		                Path name = ev.context();
		                Path child = dir.resolve(name);
		 
		                // if directory is created, and watching recursively, then
		                // register it and its sub-directories
		                if (recursive && (kind == ENTRY_CREATE)) {
		                    try {
		                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
		                            registerAll(child);
		                        }
		                    } catch (IOException x) {
		                        // ignore to keep sample readable
		                    }
		                }
		            }
		 
		            // reset key and remove from set if directory no longer accessible
		            if (!key.reset()) {

		            	keys.remove(key);
		 
		                // all directories are inaccessible
		                if (keys.isEmpty()) {
		                    break;
		                }
		            }
		        }
                return null;
            }
            
            @SuppressWarnings("unchecked")
            <T> WatchEvent<T> cast(WatchEvent<?> event) {
                return (WatchEvent<T>)event;
            }
        		 
            /**
             * Register the given directory with the WatchService
             */
            private void register(Path dir) throws IOException {
            	
            	//register the key with the watch service
                WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                
                if (trace) {
                	
                    Path prev = keys.get(key);
                    
                    if (prev == null) {
                        System.out.format("register: %s\n", dir);
                    } 
                    else {
        	            if (!dir.equals(prev))
        	                System.out.format("update: %s -> %s\n", prev, dir);
                    }
                }
                else
                	trace = true;
                
                keys.put(key, dir);
            }
        		 
            /**
             * Register the given directory, and all its sub-directories, with the
             * WatchService.
             */
            private void registerAll(final Path start) throws IOException {
                // register directory and sub-directories
                Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException
                    {
                        register(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }            
        };
    }
}
