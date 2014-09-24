package com.buddyware.treefrog.local.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import com.buddyware.treefrog.BaseModel;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;

public class LocalFileModel extends BaseModel {
	
	private final LocalWatchService watchService;

	private final ExecutorService watchServiceExecutor = createExecutor("WatchService");
	
	public LocalFileModel() {


		
		watchService = new LocalWatchService (taskMessages);

		setOnCancelled (new EventHandler() {
			@Override public void handle(Event arg0) {
System.out.println ("Cancelling watch service...");				
				watchService.cancel();
			}
		});
	}
	
	private void startWatchService() {
		
		System.out.println ("Starting watch service..");

		watchServiceExecutor.execute(watchService);	
		
		ArrayList <Path> watchPaths = new ArrayList <Path> ();
		watchPaths.add ((Path) Paths.get("/")); //System.getProperty("user.home")));		
		watchService.addPaths(watchPaths);
		
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
	
	 @Override protected Void call() throws Exception {
		 
		startWatchService();
	
		//listen for messages
		while (!isCancelled()) {
		
		//	Thread.sleep (400);
			//TaskMessage message = (TaskMessage) taskMessages.take();
			
			//System.out.println("Model received task message: " + message.getMessage() + " of type: " + message.getMessageType());
				
		}
		return null;
	 }	
}
