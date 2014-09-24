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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.event.EventHandler;

import com.buddyware.treefrog.BaseTask;
import com.buddyware.treefrog.util.TaskMessage;
import com.buddyware.treefrog.util.TaskMessage.TaskMessageType;

public final class LocalPathFinder extends BaseTask {

	private final ArrayList <Path> finderPaths = new ArrayList <Path>();
	
	private final LocalFileVisitor visitor;
	private final BooleanProperty isCancelled = new SimpleBooleanProperty (false);
	
    protected LocalPathFinder ( BlockingQueue<TaskMessage> messageQueue, ConcurrentLinkedQueue <Path> watchQueue) {
		super(messageQueue);

		visitor = new LocalFileVisitor (watchQueue);
		
		visitor.getCancelledProperty().bind(isCancelled);
		setOnCancelled(new EventHandler() {

		@Override
		public void handle(Event arg0) {
System.out.println ("Cancelling finder thread...");
		isCancelled.setValue(true);

		}
	});
	};

	@Override
    public final Void call() {
		
		System.out.println ("LocalPathFinder.call()" + finderPaths);	
		
		for (Path dir: finderPaths) {
System.out.println ("LocalPathFinder.call(): " + dir.toString());

			if (isCancelled())
				break;
			
	    	try {

		        Files.walkFileTree(dir, visitor);
	        } catch (IOException e) {
	        	enqueueMessage("IOException: " + e.getMessage() + "\n" + e.getStackTrace().toString(),
						TaskMessageType.TASK_ERROR);
	        	e.printStackTrace();
	        }
		};
System.out.println ("exiting path finder");		
		return null;

    };
    
    public final void setPaths (ArrayList <Path> paths) {
System.out.println("finder.setPaths()" + paths);    	
    	finderPaths.addAll(paths);
    };
}
