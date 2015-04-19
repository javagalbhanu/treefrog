package com.buddyware.treefrog.view.filesystem;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.model.filesystem.FileSystem;
import com.buddyware.treefrog.model.filesystem.FileSystemProperty;
import com.buddyware.treefrog.model.filesystem.FileSystemType;
import com.buddyware.treefrog.view.filesystem.amazons3.AmazonS3Config;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class FileNodeConfig extends BaseController {
	
	@FXML private BorderPane root;
	
	@FXML private TextField node_name;
	@FXML private Button button_apply;
	@FXML private Button button_done;
	@FXML private Button button_cancel;
	
	private FileSystem mModel = null;
	
	private BooleanProperty mModelWasUpdated = new SimpleBooleanProperty();
	
	public void setModel(FileSystem model) {
		
		mModel = model;
		
		refresh();
	}
	
	@FXML
	private void initialize() {

		button_apply.setDisable(true);
		button_done.setDisable(true);

		button_done.setOnAction(new EventHandler <ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				closeDialog();
			}
			
		});
		
		button_cancel.setOnAction(new EventHandler <ActionEvent> () {

			@Override
			public void handle(ActionEvent event) {
				closeDialog();
			}
		
		});
		
		button_apply.setOnAction(new EventHandler <ActionEvent> () {

			@Override
			public void handle(ActionEvent event) {
				mModel.setProperty(FileSystemProperty.NAME, node_name.getText());
				
				button_apply.setDisable(true);
				mModelWasUpdated.set(true);
			}
			
		});

		node_name.textProperty().addListener(new ChangeListener () {

			@Override
			public void changed(ObservableValue observable, Object oldValue,
					Object newValue) {
				
				if (mModel.getName().equals(node_name.getText()))
					return;
				
				button_apply.setDisable(false);				
				
			}
			
		});
	}
	
	private void closeDialog() {
		Stage stage = (Stage) button_done.getScene().getWindow();
		stage.close();
	}
	
	@FXML
	private void dataChange() {
		button_apply.setDisable(false);
		button_done.setDisable(false);
	}
	
	public void setType (FileSystemType fs_type) {

		switch (fs_type) {
		
		case AMAZON_S3:
			root.setCenter(new AmazonS3Config());
		break;

		case LOCAL_DISK:
		break;
		
		default:
		break;
		}
	}
	
	public void addModelUpdateListener (InvalidationListener listener) {
	
		mModelWasUpdated.addListener(listener);
		
	}
	
	public void refresh () {
		
		node_name.setText(mModel.getName());
		
	}
}
