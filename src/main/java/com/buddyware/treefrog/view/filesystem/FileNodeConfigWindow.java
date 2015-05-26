package com.buddyware.treefrog.view.filesystem;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.model.filesystem.FileSystemModel;
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

public class FileNodeConfigWindow extends BaseController {
	
	@FXML private BorderPane root;
	
	@FXML private TextField node_name;
	@FXML private Button button_apply;
	@FXML private Button button_done;
	@FXML private Button button_cancel;
	
	private FileSystemModel mModel = null;
	private ConfigView mView = null;
	
	public void setModel(FileSystemModel model) {
		
		mModel = model;
		
		refresh();
	}
	
	@FXML
	private void initialize() {

		button_apply.setDisable (true);
		button_done.setDisable (true);

		button_done.setOnAction ((ActionEvent e) -> closeDialog());
		button_cancel.setOnAction ((ActionEvent e) -> closeDialog());
		
		button_apply.setOnAction ((ActionEvent e) -> {
System.out.println("serializing");
			try {
				mView.serialize();
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}

//			mModel.setProperty (FileSystemProperty.NAME, node_name.getText());
			button_apply.setDisable (true);
			button_done.setDisable (false);
		//	button_done.requestFocus();
		});
/*
		node_name.textProperty().addListener (
				(observable, oldValue, newValue) -> {
				
					if (mModel.getName().equals(node_name.getText()))
						return;
				
					button_apply.setDisable (false);				
				});
	*/	
		//Alert alert = new Alert (AlertType.CONFIRMATION);
		
		
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
			mView = new AmazonS3Config(mModel);
		break;

		case LOCAL_DISK:
		break;
		
		default:
		break;
		}
		
		root.setCenter(mView.node());
		mView.addUpdateListener (
				(observable, oldValue, newValue) -> button_apply.setDisable (false)				
		);
	}

	public void refresh () {
		node_name.setText(mModel.getName());
	}
}
