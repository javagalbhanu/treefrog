package com.buddyware.treefrog.local.model;

import com.amazonaws.services.elasticmapreduce.model.Application;
import com.buddyware.treefrog.BaseModel;
import com.buddyware.treefrog.Main;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.shape.Path;

public class LocalFileModel extends BaseModel {
	
	private final LocalWatchService watchService;
	private StringProperty serviceMessage = new SimpleStringProperty();
	
	public Main parent = null;
	
	public LocalFileModel() {
		
		watchService = new LocalWatchService();
		
		String userHome = System.getProperty("user.home");
		System.out.println("home dir = " + userHome);
		watchService.register(System.getProperty("user.home"));
	}

	public void startWatchService() {
		
		addListener (watchService.messageProperty(), serviceMessage);
		watchService.start();
	}
	
	private void addListener(ReadOnlyStringProperty remoteProperty, StringProperty localProperty) {

		remoteProperty.addListener(new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> arg0,
					String arg1, String arg2) {
	             localProperty.setValue((String) arg0.getValue());
	             System.out.println("LocalFileModel message: " + (String)arg0.getValue());
			}
	      });
	}
	
	public final String getModelMessage() { return serviceMessage.get(); }
	public StringProperty modelMessage() { return serviceMessage; }
}
