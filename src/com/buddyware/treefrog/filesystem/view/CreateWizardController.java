package com.buddyware.treefrog.filesystem.view;

import java.util.ArrayList;

import com.buddyware.treefrog.util.utils;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class CreateWizardController {

	ArrayList <Pane> wizardPages = new ArrayList <Pane> ();
	
	@FXML
	StackPane wizardStackPane;
	
    /**
     * FXML initialization requirement
     */
    @FXML
    private void initialize() {
    	
    }
    
    @FXML
    public void nextPage() {
    	wizardPages.add(
    			(Pane) wizardStackPane.getChildren().remove(
    								wizardStackPane.getChildren().size()-1
    								)
    					);	
    }
    
    @FXML
    public void prevPage() {
    	wizardStackPane.getChildren().add(wizardPages.remove(wizardPages.size()-1));
    }
    
    @FXML
    public void chooseRemote () {
    	System.out.println ("Remote target selected!");
    }
    
    @FXML
    public void chooseLocal () {
    	System.out.println ("Local target selected!");
    }    
}
