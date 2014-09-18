package com.buddyware.treefrog.local.model;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

import com.buddyware.treefrog.util.utils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class LocalFileVisitor extends SimpleFileVisitor<Path> {
	
	private final BooleanProperty isCancelled = new SimpleBooleanProperty();
	private final Queue <Path> watchQueue;
	private final ArrayList <Path> watchPaths = new ArrayList <Path> ();
	private HashMap<String, String> exclusionsMap = null;
	
	private final Path rootPath = 
					Paths.get(System.getProperty("user.home") + "/bucketsync");
	
	LocalFileVisitor (Queue <Path> queue) {
		this.watchQueue = queue;
	}
	
	@Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
        throws IOException
    {

		if (exclusionsMap.containsKey(dir.toString())) {
			System.err.println ("Skipping " + dir.toString());			
			return FileVisitResult.SKIP_SUBTREE;
		}

		this.watchQueue.add(dir);
		this.watchPaths.add(dir);

		if (isCancelled.get()) {
			return FileVisitResult.TERMINATE;
		}
		return FileVisitResult.CONTINUE;
    }
	
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
			throws IOException {
		
		if (attrs.isSymbolicLink()) {
			//unless this is link is in the bucket sync root folder, don't
			//save the link's target path.
			if (!rootPath.equals(file.getParent()))
				return FileVisitResult.CONTINUE;
			
			String linkPath = "";

			try {
			linkPath = file.toRealPath().toString();
			} catch (IOException e) {
				return FileVisitResult.CONTINUE;
			}
			System.out.println ("found symlink: " + linkPath);
			//if the link target is valid, save it
			this.watchPaths.add(Paths.get(linkPath));
		}	
		if (exclusionsMap.containsKey(file.toString())) {
			return FileVisitResult.TERMINATE;			
		}
		
		if (isCancelled.get()) {
			return FileVisitResult.TERMINATE;
		}
		
		return FileVisitResult.CONTINUE;
	}
    
    public BooleanProperty getCancelledProperty() { return isCancelled; };
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException e)
        throws IOException {
		System.err.println ("FileVisit fail" + file.toString());				

        if (!exclusionsMap.containsKey(file.toString())) {
        	return FileVisitResult.SKIP_SUBTREE;
        	
        } else  if (e instanceof FileSystemLoopException) {
    		 utils.appendToFile(utils.exlusionsFilepath, file.toString());
    		 
    	 } else if (e instanceof AccessDeniedException) {
    		 utils.appendToFile(utils.exlusionsFilepath, file.toString());
    		 
    	 } else {
    		 utils.appendToFile(utils.exlusionsFilepath, file.toString());
    		 e.printStackTrace();
    	 }
    	 
        return FileVisitResult.SKIP_SUBTREE;
    }	
    
    public ArrayList <Path> getPaths() {
    	return watchPaths;
    }
    
    public void setExclusionsMap (HashMap <String, String> map) {
    	exclusionsMap = map;
    }
}
