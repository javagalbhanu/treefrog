package com.buddyware.treefrog.util;

import java.io.IOException;

import com.buddyware.treefrog.Main;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class utils {

	public static Stage createDialogStage (String title, Modality modality, Stage parent) {

		try {
		    // Load the fxml file and create a new stage for the popup dialog.
		    FXMLLoader loader = new FXMLLoader();
		    loader.setLocation(Main.class.getResource("util/FileDialog.fxml"));
		    BorderPane page = (BorderPane) loader.load();
		    
			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle(title);
			dialogStage.initModality(modality);
			dialogStage.initOwner(parent);
			Scene scene = new Scene (page);
			dialogStage.setScene (scene);

		return dialogStage;
		
		} catch (IOException e) {
		    e.printStackTrace();
		    return null;
		}
	}
}