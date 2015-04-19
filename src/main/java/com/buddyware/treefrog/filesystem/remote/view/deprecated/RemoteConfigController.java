package com.buddyware.treefrog.filesystem.remote.view.deprecated;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.filesystem.remote.model.deprecated.RemoteModel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

public class RemoteConfigController extends BaseController {

	private File filetoupload = null;
	private RemoteModel AWSConnection = null;
	final FileChooser fileChooser = new FileChooser();

	@FXML
	private TextField FileNameDialog;
	@FXML
	private ListView BucketList;

	/**
	 * FXML initialization requirement
	 */
	@FXML
	private void initialize() {
	}

	// Connect to S3 Button //
	@FXML
	private void on_click() {
		AWSConnection = new RemoteModel();
		ArrayList<String> ListOfBuckets = AWSConnection.ListBuckets();
		ObservableList<String> ObservableListOfBuckets = FXCollections
				.observableList(ListOfBuckets);
		BucketList.setItems(ObservableListOfBuckets);

	}

	// Browse Button //
	@FXML
	private void ChooseSingleFile() {
		filetoupload = fileChooser.showOpenDialog(super.parentStage);
		if (filetoupload != null) {
			System.out.println(filetoupload);

			FileNameDialog.setText(filetoupload.toString());
		}
	};

	// Undefined Button //
	@FXML
	private void ChooseMultipleFiles() {
		List<File> list = fileChooser.showOpenMultipleDialog(super.parentStage);
		if (list != null) {
			for (File file : list) {
				// openFile(file);
			}
		}
	};

	// Upload Button //
	@FXML
	private void UploadFile() {
		if (filetoupload != null) {
			String bucketname = null;

			try {
				bucketname = BucketList.getSelectionModel().getSelectedItem()
						.toString();
			} catch (Exception e) {
				System.out.println("Pick a bucket");
			}
			;

			if (bucketname != null) {
				AWSConnection.transferFile(bucketname, filetoupload, null);
				JOptionPane.showMessageDialog(null, "File Uploaded!",
						"Success", JOptionPane.WARNING_MESSAGE);

			}
		}
	};

	@FXML
	private void send_test_file() {

	}

}
