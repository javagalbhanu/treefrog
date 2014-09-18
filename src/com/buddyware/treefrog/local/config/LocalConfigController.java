package com.buddyware.treefrog.local.config;

import java.nio.file.Path;
import java.util.ArrayList;

import com.buddyware.treefrog.BaseController;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeView;

public class LocalConfigController extends BaseController {

	@FXML
	private TreeView fileTree;
	
	@FXML
	private ListView fileList;
	
    /**
     * FXML initialization requirement
     */
    @FXML
    private void initialize() {
    	mMain.getLocalFileModel().setOnPathsFound(new ChangeListener <ArrayList <Path> >() {

			@Override
			public void changed(
					ObservableValue<? extends ArrayList<Path>> arg0,
					ArrayList<Path> arg1, ArrayList<Path> arg2) {
				System.out.println ("Found " + arg2.size() + " paths");
/*				
				for (Path p: arg2)
					System.out.println (p.toString());
	*/			
			}
    		
    	});
    }
    
    @FXML
    public void start() {
    	mMain.getLocalFileModel().startWatchService();
    }
}
