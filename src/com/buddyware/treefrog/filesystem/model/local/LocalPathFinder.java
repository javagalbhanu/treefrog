package com.buddyware.treefrog.filesystem.model.local;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

	private final static String TAG = "LocalPathFinder";
	private final ArrayList <Path> finderPaths = new ArrayList <Path>();
	
	private final ArrayList <Path> foundPaths = new ArrayList <Path> ();
	
	private final LocalFileVisitor visitor;
	private final BooleanProperty isCancelled = new SimpleBooleanProperty (false);
	
    protected LocalPathFinder () {

    	super();

		visitor = new LocalFileVisitor ();
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

		foundPaths.clear();
		
		for (Path path: finderPaths) {
System.out.println(TAG + ".call()\n\t" + "Finding on " + path);
			if (isCancelled())
				break;

			visitor.reset();
			
	    	try {

	    		if (Files.isSymbolicLink(path))
	    			path = Files.readSymbolicLink(path);
	    		
	    		Files.walkFileTree(path,  visitor);
	    		
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }

			foundPaths.addAll(visitor.getPaths());	    	
		};

		return null;
    };
    
    public final ArrayList <Path> getPaths() {
    	
    	return foundPaths;
    }
    
    public final void setPaths (ArrayList <Path> paths) {
    	finderPaths.clear();
    	finderPaths.addAll(paths);
    };
}
