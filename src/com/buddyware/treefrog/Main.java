package com.buddyware.treefrog;
	
import java.io.File;
import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import com.buddyware.treefrog.filesystem.model.FileSystem;
import com.buddyware.treefrog.filesystem.model.FileSystemFactory;
import com.buddyware.treefrog.filesystem.model.FileSystemType;
import com.buddyware.treefrog.syncbinding.model.SyncBindingModel;
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

	private final FileSystem mLocalDiskModel = FileSystemFactory
			.buildFileSystem(FileSystemType.LOCAL_DISK, 
								"/media/joel/New Volume/bucketsync"
						);
	
	private final SyncBindingModel mBindingModel = new SyncBindingModel();
	
	@Override
	public void start(Stage primaryStage) {

		BaseController.mMain = this;
		this.primaryStage=primaryStage;
		
		utils.<BorderPane, BaseController> loadFxml ("RootLayout.fxml", primaryStage, null);
		primaryStage.setTitle ("BucketSync");

		mBindingModel.bindFilesystems(mSourceModel, mLocalDiskModel, null);
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
		mLocalDiskModel.shutdown();
	}
	
	public FileSystem getLocalFileModel() {
		return mSourceModel;
	}
	
	public void startFileSystems() {
		mSourceModel.start();
		mLocalDiskModel.start();
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
	
	public static <S extends Pane, T extends BaseController> T loadFxml 
	(String resource, Stage parentStage, BaseController controller) {
		
		//creates a new fxml object, returning the controller (if assigned)
		//parent stage contains the created scene and it's root layout of the generic type
			
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation (Main.class.getResource(resource));
			S layout = null;
			
			try {
				layout = (S) fxmlLoader.load();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			//scene containing root layout
			Scene sc = new Scene (layout);

			parentStage.setScene (sc);
			parentStage.show();
		
			if (controller != null)
				fxmlLoader.setController(controller);
			else {
				if (fxmlLoader.getController() instanceof BaseController) {
					controller = fxmlLoader.getController();
					controller.setParentStage(parentStage);
				}
				else
					controller = null;
			}
			
			return (T) controller;
	};	
}
