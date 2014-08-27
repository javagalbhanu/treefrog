package com.buddyware.treefrog;
	
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import com.buddyware.treefrog.util.utils;

public class Main extends Application {
	
	private Stage primaryStage;
	private BorderPane rootLayout;
	
	
	@Override
	public void start(Stage primaryStage) {
		
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle ("BucketSync");
		
		//static reference for all controllers to provide access back to main, if needed.
		BaseController.mMain = this;
		/*
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,600,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		} catch(Exception e) {
			e.printStackTrace();
		}*/
		
		utils. <BorderPane, BaseController> loadFxml ("RootLayout.fxml", this.primaryStage);
	}
	
	@FXML
	public void showDirectoryWatchServiceView() {
		
	}
	
	@FXML
	public void showJavaFxDemoView() {
	
		try {
			
			//load the aws demo code view
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation (Main.class.getResource("awsdemo/view/AwsDemoView.fxml"));
			AnchorPane awsDemoView = (AnchorPane) loader.load();
			
	//		AwsDemoController ctl = loader.getController();
			
	//		ctl.setParentStage (primaryStage);
			
			//set the view in the center of the root layout
			rootLayout.setCenter (awsDemoView);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
	}
	
	public Stage getPrimaryStage() {
		
		return primaryStage;
		
	}
	
	public static void main(String[] args) {
		
		launch(args);
		
	}
}
