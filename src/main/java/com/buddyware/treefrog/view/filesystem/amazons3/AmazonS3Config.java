package com.buddyware.treefrog.view.filesystem.amazons3;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.amazonaws.auth.AWSCredentials;
import com.buddyware.treefrog.utils;
import com.buddyware.treefrog.model.IniFile;
import com.buddyware.treefrog.model.filesystem.FileSystem;
import com.buddyware.treefrog.model.filesystem.amazons3.AmazonS3CredentialProvider;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.AnchorPane;

public class AmazonS3Config extends AnchorPane{
		
	@FXML private AnchorPane root;
	@FXML private ComboBox<String> cboProfiles;
	@FXML private TextField txtAccessKeyId;
	@FXML private TextArea txtSecretAccessKey;
	@FXML private TextField txtCredRotation;
	
	private IniFile mCredFile = null;
	private FileSystem mFileSystem = null;
	private CredentialWizard mWizard = new CredentialWizard();
	
	private final Map <String, AWSCredentials> mCreds = new HashMap <String, AWSCredentials> ();
	
	private final AmazonS3CredentialProvider mProvider = new AmazonS3CredentialProvider();
	
	public AmazonS3Config(FileSystem fs) {
		
		mFileSystem = fs;
		loadWidget();
	}
	
	@FXML
	private void initialize() {
		
		try {
			mCredFile = new IniFile(utils.getApplicationDataPath() + "/.bucketsync/credentials.cfg");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean hasRootCreds = !(mProvider.getCredentials().isEmpty());
		boolean hasAccessCreds = !(mFileSystem.getCredentialId() == null);
		boolean validAccessCreds = false;
		
		if (hasAccessCreds)
			validAccessCreds = !(mCredFile.getEntries().get(mFileSystem.getCredentialId()) == null);
		
		if (!validAccessCreds)
			mWizard.alertMissingCredentials();
		
		if (!hasRootCreds)
			if (mWizard.chooseAutoSetup())
				mWizard.chooseRootCredentialProfile(mProvider.getCredentialProfiles());
			
		populate();
			
	}
	
	private void runWizard() {
		
	}
	
	private void populate() {
		/*
		
		for (String profile: mModel.getCredentials().keySet())
			cboProfiles.getItems().add(profile);
		
		txtCredRotation.setText("90");
		*/		
	}
	

	@FXML
	private void selectProfile() {
		
		String item = cboProfiles.getSelectionModel().getSelectedItem();
		
		if (item == null)
			return;
		
	//	txtAccessKeyId.setText(
				//mModel.getCredentials().get(item)
				//.getAWSAccessKeyId()
	//			);
		
	//	txtSecretAccessKey.setText(
			//	mModel.getCredentials().get(item)
			//	.getAWSSecretKey()
	//			);
		
	}
	
	private void loadWidget() {
		
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource("/AmazonS3Config.fxml")
				);
		
		fxmlLoader.setRoot(this); 
		fxmlLoader.setController(this);

		try { 
			fxmlLoader.load();
        
		} catch (IOException exception) {
		    throw new RuntimeException(exception);
		}		
	}
	
	private void testAlert() {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation Dialog with Custom Actions");
		alert.setHeaderText("Look, a Confirmation Dialog with Custom Actions");
		alert.setContentText("Choose your option.");
	
		ButtonType buttonTypeOne = new ButtonType("One");
		ButtonType buttonTypeTwo = new ButtonType("Two");
		ButtonType buttonTypeThree = new ButtonType("Three");
		ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
	
		alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeThree, buttonTypeCancel);
	
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == buttonTypeOne){
		    System.out.println("One");
		} else if (result.get() == buttonTypeTwo) {
			System.out.println("Two");
		} else if (result.get() == buttonTypeThree) {
		    System.out.println("Three");
		} else {
		    System.out.println("Cancel");
		}	
	}	
}