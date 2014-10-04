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
    private final ObjectProperty <ArrayDeque <LocalWatchPath> > addedPaths = 
					new SimpleObjectProperty <ArrayDeque <LocalWatchPath> > ();

    //returns the paths last removed from the watch service
    private final ObjectProperty <ArrayDeque <LocalWatchPath> > removedPaths = 
					new SimpleObjectProperty <ArrayDeque <LocalWatchPath> > ();

    
    //total list of paths currently watched by the watch service
    private final ArrayDeque <LocalWatchPath> watchPaths = 
    										new ArrayDeque <LocalWatchPath> ();
       
	public LocalFileModel() {
		
	    //rootpath points to the bucketsync directory in user's home
	    LocalWatchPath.setRootPath (
	    			Paths.get(System.getProperty("user.home") + "/bucketsync")
	    );
	    
		watchService = new LocalWatchService (taskMessages);
		
		addedPaths.bind (watchService.addedPaths());
		removedPaths.bind (watchService.removedPaths());
		
		setOnPathsAdded (new ChangeListener <ArrayDeque <LocalWatchPath> >() {

			@Override
			public void changed( 
				ObservableValue<? extends ArrayDeque<LocalWatchPath> > changes,
				ArrayDeque<LocalWatchPath> oldValues, 
				ArrayDeque<LocalWatchPath> newValues) {
			
				watchPaths.addAll(newValues);
			}
		});
		
		setOnPathsRemoved (new ChangeListener <ArrayDeque <LocalWatchPath> >() {

			@Override
			public void changed(
				ObservableValue<? extends ArrayDeque<LocalWatchPath> > changes,
				ArrayDeque<LocalWatchPath> oldValues, 
				ArrayDeque<LocalWatchPath> newValues) {
				
				//pop paths from deque and remove them from the watch paths
				while (!newValues.isEmpty())
					watchPaths.remove (newValues.remove());
			}
		});

		startWatchService();
	}
	
	public void shutdown() {
		watchService.cancel();
	}
	
	public Path getRootPath() {
		return LocalWatchPath.getRootPath();
	};
	
	public void setOnPathsAdded 
					(ChangeListener <ArrayDeque <LocalWatchPath> > listener) {
		addedPaths.addListener(listener);
	};

	public void setOnPathsRemoved 
					(ChangeListener <ArrayDeque <LocalWatchPath> > listener) {
		removedPaths.addListener(listener);
	};
	
	public ArrayDeque<LocalWatchPath> getWatchedPaths () {
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
