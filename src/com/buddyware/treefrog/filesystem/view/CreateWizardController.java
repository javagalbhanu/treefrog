package com.buddyware.treefrog.filesystem.view;

import java.util.ArrayList;
import java.util.Collection;

import com.buddyware.treefrog.util.utils;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;

public class CreateWizardController {

	ArrayList <Pane> wizardPages = new ArrayList <Pane> ();
	
	@FXML
	StackPane wizardStackPane;
	
	@FXML
	ComboBox comboSyncTargets;
	
	@FXML
	Button syncToCloud;
	
	@FXML
	Button syncToComputer;
	
    /**
     * FXML initialization requirement
     */
    @FXML
    private void initialize() {
        comboSyncTargets.setCellFactory(
        		new Callback<ListView<String>, ListCell<String>>() {
        			
        			@Override public ListCell<String> call(ListView<String> param) {
        				
                        final ListCell<String> cell = new ListCell<String>() {
                        	
                            { super.setPrefWidth(100); }
                            
                            @Override public void updateItem(String item, 
                                boolean empty) {
                            	
                            		super.updateItem(item, empty);
                            		
                                    if (item != null) {
                                    	
                                        setText(item);
                                        this.setFont(Font.font("DejaVu Sans", 24));
                                    }
                                    
                                    else {
                                        setText(null);
                                    }
                                }
                    };
                    return cell;
                }
            });    	
    }
    
    @FXML
    public void nextPage() {
    	
    	int lastPageIdx = wizardStackPane.getChildren().size() - 1;
    	
    	if (lastPageIdx < 0) return;
    	
    	Pane lastPage = (Pane) wizardStackPane.getChildren().remove(lastPageIdx);
    	
    	wizardPages.add(lastPage);
    }
    
    @FXML
    public void prevPage() {
    	
    	int lastPageIdx = wizardPages.size() - 1;
    	
    	if (lastPageIdx < 0) return;
    	
    	Pane lastPage = wizardPages.get(lastPageIdx);
    	
    	wizardStackPane.getChildren().add(lastPage);
    }
    
    private void initComboBox (Collection list) {
    	
    	comboSyncTargets.getItems().clear();
    	//comboSyncTargets.setPlaceholder("No valid destinations found...");
    	comboSyncTargets.getItems().addAll(list);
    	comboSyncTargets.setPromptText("Select...");
    	//comboSyncTargets.getItems().add(0, "Select...");
    	//comboSyncTargets.getSelectionModel().select(0);
    }
    @FXML
    public void chooseRemote () {
    	initComboBox(
    			FXCollections.observableArrayList(utils.getRemoteProviders())
    			);
    	nextPage();
    }
    
    @FXML
    public void chooseLocal () {
    	initComboBox(
    			FXCollections.observableArrayList(utils.getVolumes())
    			);
    	nextPage();
    }    
}
