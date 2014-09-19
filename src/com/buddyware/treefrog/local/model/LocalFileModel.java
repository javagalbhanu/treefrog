package com.buddyware.treefrog.local.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Queue;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import com.buddyware.treefrog.BaseModel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;


public class LocalFileModel extends BaseModel {
	
	private final LocalWatchService watchService;

	private final Queue <Path> pathQueue = new ConcurrentLinkedQueue <Path> ();
	
	private final ExecutorService watchServiceExecutor = 
										createExecutor("WatchService", true);
    
    private final ObjectProperty <ArrayList <Path>> pathsFound = 
								new SimpleObjectProperty <ArrayList <Path>> ();
    
    private final ArrayList <Path> watchedPaths = new ArrayList <Path>();
    
    private final Path rootPath = 
    				Paths.get(System.getProperty("user.home") + "/bucketsync");
    
	public LocalFileModel() {

		watchService = new LocalWatchService (taskMessages, rootPath);
		pathsFound.bind(watchService.pathsFound());
		
		//startWatchService();
	}
	
	public void shutdown() {
		watchService.cancel();
	}
		
	public void setOnPathsFound (ChangeListener <ArrayList <Path>> listener) {
		pathsFound.addListener(listener);
	};
	/*
	public ArrayList <Path> getWatchedPaths() {
		return watchService.getwatchedPaths();
	}*/
	
	public void startWatchService () {
		System.out.println ("starting watch service");
		watchServiceExecutor.execute(watchService);	
		watchServiceExecutor.shutdown();
	}

	public void killWatchService() {
		if (watchService.isRunning())
			watchService.cancel();
	}
	
	public ReadOnlyObjectProperty watchServiceState() {
		return watchService.stateProperty();
	}
	
	private ChangeListener<String> createListener(StringProperty localProperty) {
	
		return new ChangeListener <String> () {

			@Override
			public void changed(ObservableValue<? extends String> arg0,
					String arg1, String arg2) {
			
				localProperty.setValue (arg0.getValue());
			}
			
		};
	};
}
