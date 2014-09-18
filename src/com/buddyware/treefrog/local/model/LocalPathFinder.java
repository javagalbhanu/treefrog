package com.buddyware.treefrog.local.model;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
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

	private final ArrayList <Path> finderPaths = new ArrayList <Path>();
	private final ArrayList <Path> foundPaths = new ArrayList <Path> ();
	private final LocalFileVisitor visitor;
	private final BooleanProperty isCancelled = new SimpleBooleanProperty (false);
	private Integer visitDepth = Integer.MAX_VALUE;
	private Boolean followSymLinks = false;
	
    protected LocalPathFinder ( BlockingQueue<TaskMessage> messageQueue, Queue <Path> watchQueue) {

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

	public void setFollowLinks (Boolean flag) {
		
		if (this.isRunning())
			return;
		
		followSymLinks = flag;
	}
	
	public void setDepth (Integer depth) {
		visitDepth = depth;
	}
	
	@Override
    public final Void call() {
		
		for (Path dir: finderPaths) {

			if (isCancelled())
				break;
		
			
	    	try {
	    		if (followSymLinks) {
	    			EnumSet<FileVisitOption> opts = EnumSet.of (FileVisitOption.FOLLOW_LINKS); 

	    			Files.walkFileTree(dir, opts, visitDepth, visitor);
	    		}
	    		else {
	    			Files.walkFileTree(dir,  visitor);
	    		}
	    			
	        } catch (IOException e) {
	        	System.out.println ("IOException: " + e.getMessage() + "\n" + e.getStackTrace().toString());
	        	enqueueMessage("IOException: " + e.getMessage() + "\n" + e.getStackTrace().toString(),
						TaskMessageType.TASK_ERROR);
	        	e.printStackTrace();
	        }
		};

		foundPaths.addAll(visitor.getPaths());
		
System.out.println ("visit complete");
		return null;
    };
    
    public final ArrayList <Path> getPaths() {
System.out.println ("found " + foundPaths.size() + " paths...");
System.out.println (foundPaths.get(0).toString());
    	return foundPaths;
    }
    
    public final void setPaths (ArrayList <Path> paths) {
System.out.println ("looking for " + paths.size() + " paths...");
System.out.println (paths.get(0).toString());
    	finderPaths.addAll(paths);
    };
}
