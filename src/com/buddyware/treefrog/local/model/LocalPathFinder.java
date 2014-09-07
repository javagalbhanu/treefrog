package com.buddyware.treefrog.local.model;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.buddyware.treefrog.BaseTask;
import com.buddyware.treefrog.util.TaskMessage;
import com.buddyware.treefrog.util.TaskMessage.TaskMessageType;

public final class LocalPathFinder extends BaseTask {

	private final ArrayList <Path> finderPaths = new ArrayList <Path>();
	
	private final ConcurrentLinkedQueue <Path> watchQueue;
	private final LocalWatchService watcher;
	
    protected LocalPathFinder ( BlockingQueue<TaskMessage> messageQueue, LocalWatchService watcher) {
		super(messageQueue);
		this.watcher = watcher;
		this.watchQueue = watcher.watchQueue();
		
		// TODO Auto-generated constructor stub
	};

	@Override
    public final Void call() {
		
		for (Path dir: finderPaths) {
			
			if (isCancelled())
				break;
		
	    	try {
		        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
		            @Override
		            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
		                throws IOException
		            {
						try {
							watcher.register (dir);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		                return FileVisitResult.CONTINUE;
		            }
		        });
	        } catch (IOException e) {
	        	enqueueMessage("IOException: " + e.getMessage() + "\n" + e.getStackTrace().toString(),
						TaskMessageType.TASK_ERROR);
	        }
		};
		
		return null;

    };
    
    public final void setPaths (ArrayList <Path> paths) {
    	finderPaths.addAll(paths);
    };
}
