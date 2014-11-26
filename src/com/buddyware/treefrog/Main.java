package com.buddyware.treefrog;
	
import java.io.File;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;

import com.buddyware.treefrog.filesystem.model.FileSystem;
import com.buddyware.treefrog.filesystem.model.FileSystemFactory;
import com.buddyware.treefrog.filesystem.model.FileSystemType;
import com.buddyware.treefrog.util.ApplicationPreferences;
import com.buddyware.treefrog.util.ApplicationPreferences.PreferenceId;
import com.buddyware.treefrog.util.utils;


public class Main extends Application {

	private Stage primaryStage;
	private final ApplicationPreferences mAppPrefs = 
												new ApplicationPreferences();

	private final FileSystem mSourceModel = FileSystemFactory
					.buildFileSystem(FileSystemType.LOCAL_DISK,
										mAppPrefs.get(PreferenceId.ROOT_PATH)
									);
	
	private final FileSystem mFileSystemModel = FileSystemFactory
					.buildFileSystem(FileSystemType.FILE_SYSTEM, "");
	
	private final Hashtable <String, FileSystem> mFileSystems = 
			new Hashtable <String, FileSystem> ();
	
	@Override
	public void start(Stage primaryStage) {

		BaseController.mMain = this;
		this.primaryStage=primaryStage;
		
		utils. <BorderPane, BaseController> loadFxml ("RootLayout.fxml", primaryStage, null);
		primaryStage.setTitle ("BucketSync");

		
		File[] files = utils.getVolumes();
	}
	
	public Stage getPrimaryStage(){
		return this.primaryStage;
	}
	
	public static void main(String[] args) {		
		launch(args);
	}
	
	@Override
	public void stop() {
		mSourceModel.shutdown();
	}
	
	public FileSystem getLocalFileModel() {
		return mSourceModel;
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
