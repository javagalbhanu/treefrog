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
    	
    	mMain.getLocalFileModel().setOnPathsAdded(
    							new ChangeListener <ArrayDeque <Path> >() {

			@Override
			public void changed( ObservableValue<? extends ArrayDeque<Path> > changes,
					ArrayDeque<Path> oldpaths, ArrayDeque<Path> newpaths) {

				System.out.println ("Found " + newpaths.size() + " paths to add");

				while (!newpaths.isEmpty())
					addTreeItems (newpaths.remove().iterator(), fsRoot);
			}
    	});

    	mMain.getLocalFileModel().setOnPathsRemoved(
				new ChangeListener <ArrayDeque <Path> >() {

		@Override
		public void changed( ObservableValue<? extends ArrayDeque<Path> > changes,
			ArrayDeque<Path> oldpaths, ArrayDeque<Path> newpaths) {
		
			System.out.println ("Found " + newpaths.size() + " paths to remove");
		
			while (!newpaths.isEmpty())
				removeTreeItems (newpaths.remove().iterator(), fsRoot);
			}
		});
    	
    	for (Path item: mMain.getLocalFileModel().getWatchedPaths())
    		addTreeItems (item.iterator(), fsRoot);
    }
    
    private boolean removeTreeItems (Iterator pathIt, TreeItem <String> treeItem) {
    	
    	//if we've reached the end of the path, then this tree item and it's
    	//children need to be removed.
    	
    	if (!pathIt.hasNext())
    		return true;
    	
    	String pathValue = pathIt.next().toString();
    	
    	for (TreeItem <String> treeChild: treeItem.getChildren()) {
    		
    		//if the path matches, recurse
    		if (treeChild.getValue().equals (pathValue)) {
    			
    			//if the recurse returns true, the end of the path has been
    			//reached, so delete the tree item.
    			if (removeTreeItems (pathIt, treeChild))
    				treeChild.getParent().getChildren().remove(treeChild);
    				
    			return false;
    		}
    	}
    	return false;
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
