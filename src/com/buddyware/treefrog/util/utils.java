package com.buddyware.treefrog.util;

import java.io.File;
import java.io.IOException;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.Main;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
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

	public static <S extends Pane, T extends BaseController> T loadFxml (String resource, Stage parentStage) {
		
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
		
			if (fxmlLoader.getController() == null)
				System.out.println("Contorller is null");
			else
				System.out.println("Controller is something");
			
			if (fxmlLoader.getController() != null) {
				if (fxmlLoader.getController() instanceof BaseController)
					return (T) (fxmlLoader.getController());
			}
			
			return null;
	};
}