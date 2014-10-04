package com.buddyware.treefrog.local.model;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.event.EventHandler;

import com.buddyware.treefrog.BaseTask;
import com.buddyware.treefrog.util.TaskMessage;
import com.buddyware.treefrog.util.TaskMessage.TaskMessageType;
import com.buddyware.treefrog.util.utils;

public final class LocalPathFinder extends BaseTask {

	private final ArrayList <LocalWatchPath> finderPaths = 
											new ArrayList <LocalWatchPath>();
	
	private final ArrayDeque <Path> foundPaths = 
											new ArrayDeque <Path> ();
	
	private final LocalFileVisitor visitor;
	private final BooleanProperty isCancelled = new SimpleBooleanProperty (false);
	
    protected LocalPathFinder ( BlockingQueue<TaskMessage> messageQueue, 
    												Queue <Path> watchQueue) {

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

		for (LocalWatchPath path: finderPaths) {

			if (isCancelled())
				break;
				
	    	try {
	    		Files.walkFileTree(path.toCanonicalPath(),  visitor);
	    		
	        } catch (IOException e) {
	        	System.out.println ("IOException: " + e.getMessage() + "\n" + e.getStackTrace().toString());
	        	enqueueMessage("IOException: " + e.getMessage() + "\n" + e.getStackTrace().toString(),
						TaskMessageType.TASK_ERROR);
	        	e.printStackTrace();
	        }
		};

		foundPaths.addAll(visitor.getPaths());

		return null;
    };
    
    public final ArrayDeque <LocalWatchPath> getPaths() {
    	
    	ArrayDeque <LocalWatchPath> result = new ArrayDeque <LocalWatchPath> ();
    	
    	for (Path path: foundPaths)
    		result.add (new LocalWatchPath (path));
    	
    	return result;
    }
    
    public final void setPaths (ArrayDeque <LocalWatchPath> paths) {
    	finderPaths.addAll(paths);
    };
}
