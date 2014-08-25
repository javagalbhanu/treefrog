package com.buddyware.treefrog.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public final class FileChooserController {

	private String title;
	private Stage parent;
	
	private List<File> chosenFiles;
	private boolean isMultiSelect;
	private FileChooser fileChooser;
	private Stage fileChooserStage;
	
    public FileChooserController (String title, Stage parent) {
    	fileChooserStage = utils.createDialogStage(title, Modality.WINDOW_MODAL, parent);
        isMultiSelect = false;
    	fileChooser = new FileChooser();
    	chosenFiles = new ArrayList<File>();
    }
    
    public void setMultiSelect( Boolean rhs) {
    	isMultiSelect = rhs;
    }
    
    public void show() {
    	
    	if (!isMultiSelect) {
    		File file = fileChooser.showOpenDialog(fileChooserStage);
    		System.out.println(file.getName());

    		if (file != null)
    			chosenFiles.add(file);
    	}
    	else {
    		chosenFiles = fileChooser.showOpenMultipleDialog(fileChooserStage);
    	}
    	
    }

	public List<File> selectedFiles() {
		return chosenFiles;
	}
}