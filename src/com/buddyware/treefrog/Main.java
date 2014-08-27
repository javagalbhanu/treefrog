package com.buddyware.treefrog;
	
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import com.buddyware.treefrog.stats.StatsController;
import com.buddyware.treefrog.util.utils;

public class Main extends Application {
	
	private Stage primaryStage;
	private BorderPane rootLayout;
	
	//@FXML
	private StatsController stats_tab_view;
	
	@Override
	public void start(Stage primaryStage) {
		
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle ("BucketSync");
		
		//static reference for all controllers to provide access back to main, if needed.
		BaseController.mMain = this;

		utils. <BorderPane, BaseController> loadFxml ("RootLayout.fxml", this.primaryStage);
	}
	
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
