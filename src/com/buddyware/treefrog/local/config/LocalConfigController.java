package com.buddyware.treefrog.local.config;

import java.nio.file.Path;
import java.util.ArrayList;

import com.buddyware.treefrog.BaseController;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class LocalConfigController extends BaseController {

	@FXML
	private TreeView fileTree;
	
	@FXML
	private ListView fileList;
	
	private TreeItem <String> fsRoot = new TreeItem <String> ("Local filesystem");
	
    /**
     * FXML initialization requirement
     */
    @FXML
    private void initialize() {
    	
    	fileTree.setRoot(fsRoot);
    	
    	mMain.getLocalFileModel().setOnPathsFound(new ChangeListener <ArrayList <Path> >() {

			@Override
			public void changed(ObservableValue<? extends ArrayList<Path>> arg0,
					ArrayList<Path> arg1, ArrayList<Path> arg2) {
				
						System.out.println ("Found " + arg2.size() + " paths");
						addTreeItems (arg2);

			}
    	});
    	
    	
    }
    
    private void addTreeItems (ArrayList <Path> items) {

    	if (fsRoot.getChildren().isEmpty()) {
    		
    		for (Path item: items)
    			addTreeItem (fsRoot, item);
    	}
    	else {
    		for (Path item: items)
    			addTreeItem (fsRoot, item);
    		
    	}
    		
    }
    
    private void addTreeItem (TreeItem <String> node, Path item){
    	node.getChildren().add(new TreeItem <String> (item.toString()));
    }
    
    @FXML
    public void start() {
    	mMain.getLocalFileModel().startWatchService();
    }
}
