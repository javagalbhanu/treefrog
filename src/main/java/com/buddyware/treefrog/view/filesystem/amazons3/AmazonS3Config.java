package com.buddyware.treefrog.view.filesystem.amazons3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.identitymanagement.model.AccessKey;
import com.buddyware.treefrog.utils;
import com.buddyware.treefrog.model.IniFile;
import com.buddyware.treefrog.model.filesystem.FileSystemModel;
import com.buddyware.treefrog.model.filesystem.FileSystemProperty;
import com.buddyware.treefrog.model.filesystem.FileSystemType;
import com.buddyware.treefrog.model.filesystem.amazons3.AmazonS3CredentialProvider;
import com.buddyware.treefrog.model.filesystem.amazons3.AmazonS3Property;
import com.buddyware.treefrog.model.filesystem.amazons3.CredentialItem;
import com.buddyware.treefrog.view.filesystem.ConfigView;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

public class AmazonS3Config extends AnchorPane implements ConfigView{
		
	@FXML private AnchorPane root;
	@FXML private ComboBox<CredentialItem> cboProfiles;
	@FXML private TextField txtAccessKeyId;
	@FXML private TextField txtSecretAccessKey;
	@FXML private TextField txtCredRotation;
	@FXML private TextField txtProfileName;
	
	private IniFile mCredFile = null;
	private FileSystemModel mFileSystem = null;
	private CredentialWizard mWizard = new CredentialWizard();
	
	private final Map <String, AWSCredentials> mCreds = new HashMap <String, AWSCredentials> ();
	
	private final AmazonS3CredentialProvider mProvider = new AmazonS3CredentialProvider();
	
	public AmazonS3Config(FileSystemModel fs) {
		
		mFileSystem = fs;
		loadWidget();
	}
	
	@FXML
	private void initialize() {
		
		deserialize();
		setComboBoxCell();
		
		boolean hasRootCreds = !(mProvider.getCredentials().isEmpty());
		boolean hasAccessCreds = !(mFileSystem.getCredentialId() == null);
		boolean validAccessCreds = false;
		
		//No root credentials found
		if (!hasRootCreds) {
			mWizard.alertMissingRootCredentials();
			//TODO: either close the dialog or allow user to manually enter creds and create credentials.txt
		}
		
		if (hasAccessCreds)
			validAccessCreds = !(mCredFile.select().get(mFileSystem.getCredentialId()) == null);

		//Existing filesystem's credentials not found in the credentials.cfg file
		if (!validAccessCreds && hasAccessCreds)
			mWizard.alertMissingCredentials();
		
		//Existing file system has no access credentials.
		if (!hasAccessCreds)
			
			//Auto setup option.  User chooses root credentials (if 1+) to use to create bucketsync creds
			//User creds for bucketsync are created and serialized.
			if (mWizard.chooseAutoSetup()) {
				String result = 
						mWizard.chooseRootCredentialProfile(mProvider.getCredentialProfiles());
				
				AccessKey key = null;
				
				try {
					key = mProvider.createUserCredentials(result);
				} catch (AmazonServiceException e) {
				
					mWizard.showInvalidCredentials();
					
				}
				
				if (key == null)
					return;
				
				txtAccessKeyId.setText (key.getAccessKeyId());
				txtSecretAccessKey.setText(key.getSecretAccessKey());
				txtProfileName.setText("New Amazon S3 Profile");
				txtCredRotation.setText("90");
				
				try {
					serialize();
				} catch (IOException e) {
					// TODO DELETE BUCKETSYNC USER CREDS IF SERIALIZATION CANNOT SUCCEED!
					
					e.printStackTrace();
				}
			}		
	}
	
	public Node node () { return this; }
	
	@FXML
	private void selectProfile() {

		if (!cboProfiles.isFocused())
			return;
		
		CredentialItem item = cboProfiles.getSelectionModel().getSelectedItem();
				
		if (item == null)
			return;
		
		txtAccessKeyId.setText(item.getAccessKeyId());
		txtSecretAccessKey.setText(item.getSecretAccessKey());
		txtCredRotation.setText(item.getCredentialRotation());
		txtProfileName.setText(item.getProfileName());
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
	
	public void addUpdateListener (ChangeListener listener) {
		
		cboProfiles.valueProperty().addListener(listener);
		txtAccessKeyId.textProperty().addListener(listener);
		txtSecretAccessKey.textProperty().addListener(listener);
		txtCredRotation.textProperty().addListener(listener);
	}
	
	public void serialize() throws IOException {
		String key = txtAccessKeyId.getText();
		
		//save the type of filesystem this credential is valid for.
		mCredFile.putData(key, FileSystemProperty.TYPE.toString(), 
				FileSystemType.AMAZON_S3.toString());
		
		mCredFile.putData(key, AmazonS3Property.ACCESS_KEY_ID.toString(),
				key);

		mCredFile.putData(key, AmazonS3Property.PROFILE_NAME.toString(),
				txtProfileName.getText());

		mCredFile.putData(key, AmazonS3Property.SECRET_ACCESS_KEY.toString(),
				txtSecretAccessKey.getText());
		
		mCredFile.putData(key,  AmazonS3Property.CREDENTIAL_ROTATION.toString(),
				txtCredRotation.getText());
			
		mCredFile.write();
		
		mFileSystem.setCredentialId(key);
		mFileSystem.serialize();
	}
	
	public void deserialize() {
		
		try {
			mCredFile = new IniFile(utils.getApplicationDataPath() + "/credentials.cfg");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Map <String, Map <String, String> > selectProps = 
				mCredFile.select (FileSystemProperty.TYPE.toString(),
								  FileSystemType.AMAZON_S3.toString());
		
		//populates the drop down with profile names
		if (selectProps.size() > 0) {
		
			List <CredentialItem> profiles = new ArrayList <CredentialItem> ();
		
			for (String key: selectProps.keySet())
				profiles.add (new CredentialItem(selectProps.get(key)));
		
			cboProfiles.setItems(FXCollections.observableArrayList(profiles));
		}
		
		if (mFileSystem.getCredentialId() == null)
			return;
		
		//get properties for the credential ID assigned to the current file system
		CredentialItem ci = new CredentialItem (mCredFile.select().get(mFileSystem.getCredentialId()));
		
		if (ci.getAccessKeyId() == null)
			return;

		txtAccessKeyId.setText(ci.getAccessKeyId());
		txtSecretAccessKey.setText(ci.getSecretAccessKey());
		txtCredRotation.setText(ci.getCredentialRotation());
		txtProfileName.setText(ci.getProfileName());
		cboProfiles.setValue(ci);
	}
	
	private void setComboBoxCell() {
		
		cboProfiles.setCellFactory(
				new Callback <ListView <CredentialItem>, 
							  ListCell <CredentialItem> >() {
			@Override
			public ListCell<CredentialItem> call(
					ListView<CredentialItem> param) {
				
                return new ListCell<CredentialItem>() {

                    @Override
                    protected void updateItem(CredentialItem item, boolean empty) {
                        super.updateItem(item, empty);

                        	if (item == null || empty)
                        		return;
                       
                        	txtAccessKeyId.setText(item.getAccessKeyId());
                        	txtSecretAccessKey.setText(item.getSecretAccessKey());
                        	txtCredRotation.setText(item.getCredentialRotation());
                        	txtProfileName.setText(item.getProfileName());
                        	
                        	setText(item.getProfileName());
                        }
                    };
                };
			});		
	}
}