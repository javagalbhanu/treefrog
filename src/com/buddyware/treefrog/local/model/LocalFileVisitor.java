package com.buddyware.treefrog.local.model;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class LocalFileVisitor extends SimpleFileVisitor<Path> {
	
	private final BooleanProperty isCancelled = new SimpleBooleanProperty();
	private final ConcurrentLinkedQueue <Path> watchQueue;
	
	LocalFileVisitor (ConcurrentLinkedQueue <Path> queue) {
		this.watchQueue = queue;
	}
	
	@Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
        throws IOException
    {
		this.watchQueue.add(dir);

		if (isCancelled.get()) {
			System.out.println ("file vistor cancel detected");
			return FileVisitResult.TERMINATE;
		}
		else
			return FileVisitResult.CONTINUE;
    }
    
    public BooleanProperty getCancelledProperty() { return isCancelled; };
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException e)
        throws IOException {
        System.err.printf("Visiting failed for %s\n", file);

        return FileVisitResult.SKIP_SUBTREE;
    }	
}
