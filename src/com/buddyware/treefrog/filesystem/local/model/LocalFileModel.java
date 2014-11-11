package com.buddyware.treefrog.filesystem.local.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import com.buddyware.treefrog.BaseModel;
import com.buddyware.treefrog.filesystem.IFileSystem;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;


public class LocalFileModel extends BaseModel implements IFileSystem {
	
	//watchService task and associated executor
	private final LocalWatchService watchService;

	private final ExecutorService watchServiceExecutor = 
										createExecutor("WatchService", true);
	
	private SimpleListProperty <String> mWatchPathsAdded;
	private SimpleListProperty <String> mWatchPathsRemoved;
	
	public LocalFileModel() {
		
	    //rootpath points to the bucketsync directory in user's home
	    LocalWatchPath.setRootPath (
	    			Paths.get(System.getProperty("user.home") + "/bucketsync")
	    );
	    
		watchService = new LocalWatchService ();
		
		mWatchPathsAdded = watchService.addedPaths();
		mWatchPathsRemoved = watchService.removedPaths();
	}
	
	public Path getRootPath() {
		return LocalWatchPath.getRootPath();
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
	}
	
	@Override
	public void start() {
		startWatchService();		
	}
	
	@Override
	public void shutdown() {
		watchService.cancel();
	}	

	@Override
	public void serialize(String filepath) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deserialize(String filepath) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOnPathsAdded (ListChangeListener <String> changeListener) {
		mWatchPathsAdded.addListener(changeListener);
	};

	@Override
	public void setOnPathsRemoved (ListChangeListener <String> listener) {
		mWatchPathsRemoved.addListener(listener);
	};

	@Override
	public void setOnPathsChanged(ListChangeListener<String> listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeOnPathsAdded(ListChangeListener<String> listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeOnPathsRemoved(ListChangeListener<String> listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeOnPathsChanged(ListChangeListener<String> listener) {
		// TODO Auto-generated method stub
		
	};
}
