package com.buddyware.treefrog.util;

import java.io.IOException;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.Main;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class utils {

	public static Stage createDialogStage (String title, Modality modality, Stage parent) {

		try {
		    // Load the fxml file and create a new stage for the popup dialog.
		    FXMLLoader loader = new FXMLLoader();
		    loader.setLocation(Main.class.getResource("util/Dialog.fxml"));
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

	public static <S extends Pane, T extends BaseController> T loadFxml 
	(String resource, Stage parentStage, BaseController controller) {
		
		//creates a new fxml object, returning the controller (if assigned)
		//parent stage contains the created scene and it's root layout of the generic type
			
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation (Main.class.getResource(resource));
			S layout = null;
			
			try {
				layout = (S) fxmlLoader.load();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			//scene containing root layout
			parentStage.setScene (new Scene (layout));
			parentStage.show();
		
			if (controller != null)
				fxmlLoader.setController(controller);
			else {
				if (fxmlLoader.getController() instanceof BaseController) {
					controller = fxmlLoader.getController();
					controller.setParentStage(parentStage);
				}
				else
					controller = null;
			}
			
			return (T) controller;
	};
}