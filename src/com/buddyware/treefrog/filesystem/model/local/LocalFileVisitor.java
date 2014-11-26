package com.buddyware.treefrog.filesystem.model.local;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

import com.buddyware.treefrog.util.utils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class LocalFileVisitor extends SimpleFileVisitor<Path> {
	
	private final BooleanProperty isCancelled = new SimpleBooleanProperty();
	private final ArrayList <String> watchPaths = new ArrayList <String> ();
	
	private HashMap<String, String> exclusionsMap = null;
	
	//constructor
	LocalFileVisitor () {

	}
	
	@Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
        throws IOException
    {

		if (exclusionsMap.containsKey(dir.toString())) {
			System.err.println ("Skipping " + dir.toString());			
			return FileVisitResult.SKIP_SUBTREE;
		}

		this.watchPaths.add(dir.toString());

		if (isCancelled.get()) {
			return FileVisitResult.TERMINATE;
		}
		return FileVisitResult.CONTINUE;
    }
	
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
			throws IOException {
	
		if (attrs.isSymbolicLink())
			return FileVisitResult.CONTINUE;
		
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
    	/*	 
    	 } else if (e instanceof FileSystemNotFoundException) {
    		 utils.appendToFile(utils.exlusionsFilepath, file.toString());*/
    		 
    	 } else {
    		 utils.appendToFile(utils.exlusionsFilepath, file.toString());
    		 e.printStackTrace();
    	 }
    	 
        return FileVisitResult.SKIP_SUBTREE;
    }	
    
    public ArrayList  <String> getPaths() {  	
    	return watchPaths;
    }
    
    public void setExclusionsMap (HashMap <String, String> map) {
    	exclusionsMap = map;
    }
}
