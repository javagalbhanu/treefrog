package com.buddyware.treefrog;
	
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;

import com.buddyware.treefrog.local.model.ListPropertyTest;
import com.buddyware.treefrog.local.model.LocalFileModel;
import com.buddyware.treefrog.util.TaskMessage;
import com.buddyware.treefrog.util.utils;


public class Main extends Application {

	private final LocalFileModel localModel = new LocalFileModel();
	private Stage primaryStage;

	@Override
	public void start(Stage primaryStage) {	
		BaseController.mMain = this;
			
		
		utils. <BorderPane, BaseController> loadFxml ("RootLayout.fxml", primaryStage, null);
		primaryStage.setTitle ("BucketSync");
		this.primaryStage=primaryStage;
		localModel.start();
	}
	
	public Stage getPrimaryStage(){
		return this.primaryStage;
	}
	
	public static void main(String[] args) {		
		launch(args);
	}
	
	public LocalFileModel getLocalFileModel() {
		return localModel;
	}
	
	@Override
	public void stop() {
		localModel.shutdown();
	}
	
	public ArrayList <TaskMessage> pollLocalFileModel() {
		return new ArrayList <TaskMessage>(); //localModel.pollMessages();
	}
	protected ExecutorService createExecutor(final String name) {
		 
		 ThreadFactory factory = new ThreadFactory() {
			 
			 @Override public Thread newThread(Runnable r) {
				 Thread t = new Thread(r);
				 t.setName(name);
				 t.setDaemon(true);
				 return t;
			 }
		 };
		 
		 return Executors.newSingleThreadExecutor(factory);
	} 	
}
