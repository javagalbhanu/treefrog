package com.buddyware.treefrog.local.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
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

	private final ExecutorService watchServiceExecutor = 
										createExecutor("WatchService", true);
    
    private final ObjectProperty <ArrayDeque <Path> > pathsFound = 
								new SimpleObjectProperty <ArrayDeque <Path>> ();
    
    private final ArrayDeque <Path> watchPaths = new ArrayDeque <Path> ();
    private final Path rootPath = 
    				Paths.get(System.getProperty("user.home") + "/bucketsync");
    
	public LocalFileModel() {

		watchService = new LocalWatchService (taskMessages, rootPath);
		pathsFound.bind(watchService.pathsFound());
		
		setOnPathsFound (new ChangeListener <ArrayDeque <Path> >() {

			@Override
			public void changed( ObservableValue<? extends ArrayDeque<Path> > arg0,
				ArrayDeque<Path> arg1, ArrayDeque<Path> arg2) {
			
				System.out.println ("Adding " + arg2.size() + " paths to model deque");
				
				watchPaths.addAll(arg2);
			}
		});
		
		watchService.initializeWatchPaths();
		//startWatchService();
	}
	
	public void shutdown() {
		watchService.cancel();
	}
		
	public void setOnPathsFound (ChangeListener <ArrayDeque <Path> > listener) {
		pathsFound.addListener(listener);
	};

	public ArrayDeque<Path> getWatchedPaths () {
		return watchPaths;
	};
	
	public void startWatchService () {
		
		if (watchServiceExecutor.isShutdown())
			return;
		
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
