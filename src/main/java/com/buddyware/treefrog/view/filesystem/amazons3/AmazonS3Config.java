package com.buddyware.treefrog.view.filesystem.amazons3;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.auth.AWSCredentials;
import com.buddyware.treefrog.model.filesystem.amazons3.AmazonS3CredentialsModel;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class AmazonS3Config extends AnchorPane{
		
	@FXML private AnchorPane root;
	@FXML private ComboBox<String> cboProfiles;
	@FXML private TextField txtAccessKeyId;
	@FXML private TextArea txtSecretAccessKey;
	@FXML private TextField txtCredRotation;
	
	private final Map <String, AWSCredentials> mCreds = new HashMap <String, AWSCredentials> ();
	
	private final AmazonS3CredentialsModel mModel = new AmazonS3CredentialsModel();
	
	public AmazonS3Config() {
		loadWidget();
	}
	
	@FXML
	private void initialize() {
		
		for (String profile: mModel.getCredentials().keySet())
			cboProfiles.getItems().add(profile);
		
		txtCredRotation.setText("90");
	}
	
	@FXML
	private void selectProfile() {
		
		String item = cboProfiles.getSelectionModel().getSelectedItem();
		
		if (item == null)
			return;
		
		txtAccessKeyId.setText(
				mModel.getCredentials().get(item)
				.getAWSAccessKeyId()
				);
		
		txtSecretAccessKey.setText(
				mModel.getCredentials().get(item)
				.getAWSSecretKey()
				);
		
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
}