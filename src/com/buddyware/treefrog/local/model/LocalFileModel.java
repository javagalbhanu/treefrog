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
	
	//watchService task and associated executor
	private final LocalWatchService watchService;

	private final ExecutorService watchServiceExecutor = 
										createExecutor("WatchService", true);
    
	
	//property which returns the paths last added to the watch service
    private final ObjectProperty <ArrayDeque <Path> > addedPaths = 
								new SimpleObjectProperty <ArrayDeque <Path>> ();

    //returns the paths last removed from the watch service
    private final ObjectProperty <ArrayDeque <Path> > removedPaths = 
    							new SimpleObjectProperty <ArrayDeque <Path>> ();

    
    //total list of paths currently watched by the watch service
    private final ArrayDeque <Path> watchPaths = new ArrayDeque <Path> ();
    
    //rootpath points to the bucketsync directory in user's home
    private final Path rootPath = 
    				Paths.get(System.getProperty("user.home") + "/bucketsync");
    
	public LocalFileModel() {

		watchService = new LocalWatchService (taskMessages, rootPath);
		
		addedPaths.bind (watchService.addedPaths());
		removedPaths.bind (watchService.removedPaths());
		
		setOnPathsAdded (new ChangeListener <ArrayDeque <Path> >() {

			@Override
			public void changed( ObservableValue<? extends ArrayDeque<Path> > arg0,
				ArrayDeque<Path> arg1, ArrayDeque<Path> arg2) {
			
				watchPaths.addAll(arg2);
			}
		});
		
		setOnPathsRemoved (new ChangeListener <ArrayDeque <Path> >() {

			@Override
			public void changed(
					ObservableValue<? extends ArrayDeque<Path>> arg0,
					ArrayDeque<Path> arg1, ArrayDeque<Path> arg2) {
				
				//pop paths from deque and remove them from the watch paths
				while (!arg2.isEmpty())
					watchPaths.remove (arg2.remove());
			}
		});
		
		watchService.initializeWatchPaths();
		//startWatchService();
	}
	
	public void shutdown() {
		watchService.cancel();
	}
		
	public void setOnPathsAdded (ChangeListener <ArrayDeque <Path> > listener) {
		addedPaths.addListener(listener);
	};

	public void setOnPathsRemoved (ChangeListener <ArrayDeque <Path> > listener) {
		removedPaths.addListener(listener);
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
