package com.buddyware.treefrog.filesystem.view;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.filesystem.FileSystemType;
import com.buddyware.treefrog.filesystem.view.amazons3.AmazonS3Config;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class FileSystemConfig extends BaseController {
	
	@FXML private BorderPane root;
	
	@FXML
	private void initialize() {
	}
	
	public void setType (FileSystemType fs_type) {

		switch (fs_type) {
		
		case AMAZON_S3:
			root.setCenter(new AmazonS3Config());
		break;
		
		case SOURCE_DISK:
		case LOCAL_DISK:
		break;
		
		default:
		break;
		}
	}
}
