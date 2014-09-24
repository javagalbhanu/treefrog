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
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import com.buddyware.treefrog.BaseService;

import javafx.concurrent.Task;

public class LocalWatchService_old extends BaseService {

    private WatchService watcher;
    private Path watchDirectory;
    
    private final Map<WatchKey,Path> keys = new HashMap<WatchKey, Path>();
    
    private boolean recursive = true;
    private boolean trace = false;
    
    private long adeCounter = 0;
    
    public LocalWatchService_old() {

    	try {
			this.watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Override 
    public void start() {
    	
    	super.start();
    }
    
    public void setDirectory (Path watchpath) {
    	watchDirectory = watchpath;
    }
    
	public Map<WatchKey,Path> getKeys() {
		return keys;
	}
		 
    @SuppressWarnings("unchecked")
    <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }
	
    public void register (String dir) {
    	try {
			registerAll (Paths.get(dir));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    /**
     * Register the given directory with the WatchService
     * @throws InterruptedException 
     */
    private void register(Path dir) throws IOException, InterruptedException {
 	
    	//register the key with the watch service
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        
        if (trace) {
        	
            Path prev = keys.get(key);
            
            if (prev == null) {
            	System.out.println("Putting Activity Message 5");		                		


            	putActivityMessage ("register: " + dir + '\n');
            	System.out.println("Putting Activity Message 5 resume");		                		

            } 
            else {
	            if (!dir.equals(prev));
	            System.out.println("Putting Activity Message 6");		                		

	                putActivityMessage ("update: " + dir + " -> " + prev + "\n");
	                System.out.println("Putting Activity Message 6 resume");		                		

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
    private void registerAll(final Path start) throws InterruptedException {
        // register directory and sub-directories

    	
    	try {
	        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
	            @Override
	            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
	                throws IOException
	            {
	                try {
						register(dir);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                return FileVisitResult.CONTINUE;
	            }
	        });
        } catch (IOException e) {
        	if (e.toString().indexOf("java.nio.file.AccessDeniedException") > -1)
        		putErrorMessage ("AccessDeniedException thrown!");
        	else
        		e.printStackTrace();
        	
        	
        	adeCounter++;
        	putErrorMessage ("Total ADE's counted: " + adeCounter);
        }
    }
    
    @Override
    protected Task<Void> createTask() {
    	
        return new Task<Void>() {
        	
            @Override
            protected Void call() throws Exception {
            	
            	if (keys.size() == 0) {
         		
            		registerAll (watchDirectory);
            	}
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
System.out.println("Putting Error Message 1");		            	
		                putErrorMessage ("Null directory key encountered.");
System.out.println("Putting Error Message 1 resume");		                
		                continue;
		            }
		 
		            for (WatchEvent<?> event: key.pollEvents()) {
		            	
		                WatchEvent.Kind kind = event.kind();
		 
		                // TBD - provide example of how OVERFLOW event is handled
		                if (kind == OVERFLOW) {
System.out.println("Putting Error Message 2");		                	
		                	putErrorMessage ("Overflow encountered...");
System.out.println("Putting Error Message 2 resume");		                	
		                    continue;
		                }
		 
		                // Context for directory entry event is the file name of entry
		                WatchEvent<Path> ev = cast(event);
		                String name = ev.context().toString();
		                Path child = dir.resolve(name);
		 
		                if (kind == ENTRY_CREATE) {
		                	if (Files.isDirectory(child,  NOFOLLOW_LINKS)) {
System.out.println("Putting Activity Message 1");		                		
		                		putActivityMessage ("Directory created: " + name);
System.out.println("Putting Activity Message 1 resume");		                		
		                		if (recursive) {
		                			registerAll (child);
		                		}
		                	}
	                        else {
	                        	System.out.println("Putting Activity Message 2");		                		
	                        	
	                        	putActivityMessage ("File created: " + name);
	                        	System.out.println("Putting Activity Message 2 resume");		                		
	                        	
	                        }
		                }		                

		                if (kind == ENTRY_MODIFY) {
		                	System.out.println("Putting Activity Message 3");		                		

		                	putActivityMessage ("File modified: " + name);
		                	System.out.println("Putting Activity Message 3 resume");		                		

		                }
		                
		                if (kind == ENTRY_DELETE) {
		                	System.out.println("Putting Activity Message 4");		                		

		                	putActivityMessage ("File deleted: " + name);
		                	System.out.println("Putting Activity Message 4 reumse");		                		

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
        };
    }
}
