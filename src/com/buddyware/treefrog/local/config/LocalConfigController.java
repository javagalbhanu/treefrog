package com.buddyware.treefrog.local.config;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Iterator;

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
    	
    	mMain.getLocalFileModel().setOnPathsFound(
    							new ChangeListener <ArrayDeque <Path> >() {

			@Override
			public void changed( ObservableValue<? extends ArrayDeque<Path> > arg0,
					ArrayDeque<Path> arg1, ArrayDeque<Path> arg2) {

				System.out.println ("Found " + arg2.size() + " paths");

				for (Path item: arg2)
					addTreeItems (item.iterator(), fsRoot);
			}
    	});
    	
    	for (Path item: mMain.getLocalFileModel().getWatchedPaths())
    		addTreeItems (item.iterator(), fsRoot);
    }
    
    private void addTreeItems (Iterator pathIt, TreeItem <String> treeItem) {

    	if (!pathIt.hasNext())
    		return;
    	
    	String pathValue = pathIt.next().toString();
  
    	for (TreeItem <String> treeChild: treeItem.getChildren()) {
    		
    		//if the path matches, recurse and return
    		if (treeChild.getValue().equals(pathValue)) {
				addTreeItems (pathIt, treeChild);
    			return;
    		}
    	}
   	
    	//still here?  then this is a new path, so add it, then recurse
    	TreeItem <String> treeChild = new TreeItem <String> (pathValue); 
    	treeItem.getChildren().add (treeChild);
		addTreeItems (pathIt, treeChild);
    }
    
    @FXML
    public void start() {
    	mMain.getLocalFileModel().startWatchService();
    }
}
