package com.buddyware.treefrog;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javafx.application.Application;

import javafx.stage.Stage;

import javafx.scene.layout.BorderPane;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.util.utils;

public class Main extends Application {

	private Stage primaryStage;

	private MainController mMainController = null;
	
	@Override
	public void start(Stage primaryStage) {

		BaseController.mMain = this;
		this.primaryStage = primaryStage;

		mMainController = (MainController)
		utils.<BorderPane, BaseController> loadFxml("/RootLayout.fxml",
				primaryStage, null);
		
		primaryStage.setTitle("BucketSync");

		/*
		FileSystemModel sourcefs = mFileSystems.createModel(
				FileSystemType.SOURCE_DISK,
				mAppPrefs.get(PreferenceId.ROOT_PATH),
				"source");
		*/

	}
	
	public Stage getPrimaryStage() { return this.primaryStage; }

	public static void main(String[] args) { launch(args);	}

	@Override
	public void stop() {
		mMainController.shutdown();
	}

	protected ExecutorService createExecutor(final String name) {

		ThreadFactory factory = new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName(name);
				t.setDaemon(true);
				return t;
			}
		};

		return Executors.newSingleThreadExecutor(factory);
	}
}
