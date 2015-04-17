package com.buddyware.treefrog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import com.buddyware.treefrog.filesystem.model.FileSystem;
import com.buddyware.treefrog.filesystem.model.FileSystemModel;
import com.buddyware.treefrog.filesystem.model.FileSystemProperty;
import com.buddyware.treefrog.filesystem.view.FileSystemNodeView;
import com.buddyware.treefrog.syncbinding.model.SyncBindingModel;
import com.buddyware.treefrog.util.ApplicationPreferences;
import com.buddyware.treefrog.util.IniFile;
import com.buddyware.treefrog.util.utils;

import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import javafx.util.Pair;

public class MainController extends BaseController {

	@FXML Tab filesystem_tab;
	
	//stores general application preferences
	private final ApplicationPreferences mAppPrefs = new ApplicationPreferences();

	//collection of existing filesystems
	private final FileSystemModel mFileSystems = new FileSystemModel();

	//collection of bindings between filesystems
	private final SyncBindingModel mBindings = new SyncBindingModel();
	
	private FileSystemNodeView mFileSystemView;
	
	public MainController() {
		
		try {
			validateAppDataPath();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@FXML
	public void initialize() {
		
		mFileSystemView = new FileSystemNodeView();
		
		mFileSystemView.setFileSystemsModel (mFileSystems);
		mFileSystemView.setBindingsModel (mBindings);
		
		filesystem_tab.setContent(mFileSystemView);	
	}
	
	private void validateAppDataPath() throws IOException {
	
		/*
		 * Creates a new application data path in the user's local application directory
		 * if it doesn't already exist.  Config files go here
		 */
		File appDataPath = new File(utils.getApplicationDataPath() + "/.bucketsync");
		
		if (!appDataPath.exists())
			Files.createDirectory(appDataPath.toPath());
		
	}
	
	public void shutdown() {
		mFileSystems.shutdown();
		ThreadPool.shutdown();
	}
	
	public Stage getParentStage() {

		return parentStage;

	}
}
