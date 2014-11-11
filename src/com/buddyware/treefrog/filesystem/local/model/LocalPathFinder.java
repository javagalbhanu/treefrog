package com.buddyware.treefrog.filesystem.local.model;

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

	private final ArrayList <String> finderPaths = new ArrayList <String>();
	
	private final ArrayList <String> foundPaths = new ArrayList <String> ();
	
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

		for (String path: finderPaths) {

			if (isCancelled())
				break;
				
	    	try {
	    		Files.walkFileTree(Paths.get(path),  visitor);
	    		
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }
		};

		foundPaths.addAll(visitor.getPaths());

		return null;
    };
    
    public final ArrayList <String> getPathNames() {
    	
    	return foundPaths;
    }
    
    public final void setPaths (ArrayList <String> paths) {
    	finderPaths.addAll(paths);
    };
}
