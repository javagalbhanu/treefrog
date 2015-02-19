package com.buddyware.treefrog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.buddyware.treefrog.filesystem.FileSystemsModel;
import com.buddyware.treefrog.filesystem.view.FileSystemNodeView;
import com.buddyware.treefrog.syncbinding.model.SyncBindingModel;
import com.buddyware.treefrog.util.ApplicationPreferences;
import com.buddyware.treefrog.util.IniFile;
import com.buddyware.treefrog.util.utils;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.stage.Stage;

public class MainController extends BaseController {

	@FXML Tab filesystem_tab;
	
	//stores general application preferences
	private final ApplicationPreferences mAppPrefs = new ApplicationPreferences();

	//collection of existing filesystems
	private final FileSystemsModel mFileSystems = new FileSystemsModel();

	//collection of bindings between filesystems
	private final SyncBindingModel mBindings = new SyncBindingModel();
	
	private IniFile mIniFile = null;
		
	public MainController() {
		
		try {
			validateAppDataPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			mIniFile = openIniFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mFileSystems.deserialize(mIniFile);
		mBindings.deserialize(mIniFile, mFileSystems);		
	}

	@FXML
	public void initialize() {
		filesystem_tab.setContent(new FileSystemNodeView());
	}

	private IniFile openIniFile() throws IOException {
		
		return new IniFile
				(utils.getApplicationDataPath() + "/filesystems.ini");
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
