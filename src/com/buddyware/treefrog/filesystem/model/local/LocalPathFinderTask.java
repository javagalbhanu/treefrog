package com.buddyware.treefrog.filesystem.model.local;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import com.buddyware.treefrog.BaseTask;
import com.buddyware.treefrog.util.TaskMessage;
import com.buddyware.treefrog.util.TaskMessage.TaskMessageType;
import com.buddyware.treefrog.util.utils;

public final class LocalPathFinderTask extends BaseTask<List <Path>> {

	private final static String TAG = "LocalPathFinderTask";
	
	private final List <Path> finderPaths;
	
	private final LocalFileVisitor visitor;
	private final BooleanProperty isCancelled = new SimpleBooleanProperty (false);
	
    protected LocalPathFinderTask (List <Path> paths) {

    	super();

    	finderPaths = paths;
    	
		visitor = new LocalFileVisitor ();
		visitor.getCancelledProperty().bind(isCancelled);

		LocalFileModelScanner s = new LocalFileModelScanner(utils.exlusionsFilepath);
		
		visitor.setExclusionsMap (s.getStreamMap());
		
		setOnCancelled(arg0 -> isCancelled.setValue(true));
	};

	@Override
    public final List <Path> call() {

		List<Path> foundpaths = new ArrayList<Path> ();
		
		for (Path path: finderPaths) {
System.out.println(TAG + ".call()\n\t" + "Finding on " + path);
			if (isCancelled())
				break;

			visitor.reset();
			
	    	try {

	    		if (Files.isSymbolicLink(path))
	    			path = Files.readSymbolicLink(path);
	    		
	    		Files.walkFileTree(path,  visitor);
	    		
	    		foundpaths.addAll(visitor.getPaths());
	    		
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }
		};

		return foundpaths;
    };
}
