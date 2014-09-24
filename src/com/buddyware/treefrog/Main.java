package com.buddyware.treefrog;
	
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;

import com.buddyware.treefrog.local.model.LocalFileModel;
import com.buddyware.treefrog.util.TaskMessage;
import com.buddyware.treefrog.util.utils;


public class Main extends Application {

	private final LocalFileModel localModel = new LocalFileModel();
	
	@Override
	public void start(Stage primaryStage) {
		
		BaseController.mMain = this;
		utils. <BorderPane, BaseController> loadFxml ("RootLayout.fxml", primaryStage, null);
		primaryStage.setTitle ("BucketSync");
		
		primaryStage.setOnCloseRequest(new EventHandler() {
			
			 @Override public void handle(Event event) {
System.out.println ("cancelling localmodel");				 
			// localModel.cancel();
			 }
		});
		 
		final ExecutorService lm = createExecutor ("lm");
		lm.execute(localModel);
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public LocalFileModel getLocalFileModel() {
		return localModel;
	}
	
	@Override
	public void stop() {
		localModel.cancel();
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
