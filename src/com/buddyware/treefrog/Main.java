package com.buddyware.treefrog;
	
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.attribute.BasicFileAttributes;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;

import com.buddyware.treefrog.local.model.LocalFileModel;
import com.buddyware.treefrog.stats.StatsController;
import com.buddyware.treefrog.util.utils;

public class Main extends Application {
	
	private Stage primaryStage;
	private BorderPane rootLayout;
	
	private final StringProperty message = new SimpleStringProperty();
	
	private final LocalFileModel localModel = new LocalFileModel();
	
	@FXML private StatsController stats_view;
	//private StatsController stats_tab_view;
//	private StatsController stats_tab_view;
	//private StatsController stats_tab_view;
	
	@Override
	public void start(Stage primaryStage) {
		
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle ("BucketSync");
		/*
		localModel.modelMessage().addListener(new ChangeListener(){
			@Override
			public void changed(ObservableValue arg0, Object arg1, Object arg2) {
				System.out.println ("Value has changed!");
				System.out.println (arg0.toString());
			}
		});			
*/
		//bind to the local model's message property for updates
		message.bindBidirectional(localModel.modelMessage());
		
		
		message.addListener(new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> arg0,
					String arg1, String arg2) {
	             System.out.println("Main message: " + (String)arg0.getValue());
			}
	      });
		
		//static reference for access back to main, if needed.
		BaseController.mMain = this;
		BaseModel.mMain = this;
		
		utils. <BorderPane, BaseController> loadFxml ("RootLayout.fxml", this.primaryStage);
	}

	public void startWatchService() {
		localModel.startWatchService();
	}
	
	public final String getMessage() { return message.get(); }
	public StringProperty message() { return message; }	
	
	@FXML
	public void showStatsTabView() {
	
	}
	
	@FXML
	public void showRemoteConfigTabView() {
	
	}
	
	@FXML
	public void showLocalConfigTabView() {
		
	}
	
	@FXML
	public void showHelpTabView() {
		
	}
	
	@FXML
	public void showAboutTabView() {
		
	}
	
	public Stage getPrimaryStage() {
		
		return primaryStage;
		
	}
	
	public static void main(String[] args) {
		
		launch(args);
		
	}
}
