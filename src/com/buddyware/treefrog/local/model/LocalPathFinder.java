package com.buddyware.treefrog.local.model;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
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
import com.buddyware.treefrog.util.utils;

public final class LocalPathFinder extends BaseTask {

	private final ArrayList <Path> finderPaths = new ArrayList <Path>();
	
	private final LocalFileVisitor visitor;
	private final BooleanProperty isCancelled = new SimpleBooleanProperty (false);
	
    protected LocalPathFinder ( BlockingQueue<TaskMessage> messageQueue, ConcurrentLinkedQueue <Path> watchQueue) {

    	super(messageQueue);

		visitor = new LocalFileVisitor (watchQueue);
		visitor.getCancelledProperty().bind(isCancelled);

		LocalFileModelScanner s = new LocalFileModelScanner(utils.exlusionsFilepath);
		
		visitor.setExclusionsMap (s.getStreamMap());
		
		setOnCancelled(new EventHandler() {

		@Override
		public void handle(Event arg0) {
		isCancelled.setValue(true);

		}
	});
	};

	@Override
    public final Void call() {
		
		for (Path dir: finderPaths) {

			if (isCancelled())
				break;

			EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
			
	    	try {
		        Files.walkFileTree(dir, opts, Integer.MAX_VALUE, visitor);
	        } catch (IOException e) {
	        	enqueueMessage("IOException: " + e.getMessage() + "\n" + e.getStackTrace().toString(),
						TaskMessageType.TASK_ERROR);
	        	e.printStackTrace();
	        }
		};

		
System.out.println ("visit complete");
		return null;
    };
    
    public final void setPaths (ArrayList <Path> paths) {
    	finderPaths.addAll(paths);
    };
}
