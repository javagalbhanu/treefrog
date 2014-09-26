package com.buddyware.treefrog.local.config;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Iterator;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.local.model.LocalWatchPath;

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
	
	private LocalTreeItem fsRoot = 
					new LocalTreeItem (mMain.getLocalFileModel().getRootPath());
	
    /**
     * FXML initialization requirement
     */
    @FXML
    private void initialize() {
    	
    	fileTree.setRoot(fsRoot);
    	
    	mMain.getLocalFileModel().setOnPathsAdded(
						new ChangeListener <ArrayDeque <LocalWatchPath> >() {

			@Override
			public void changed( 
				ObservableValue<? extends ArrayDeque <LocalWatchPath> > changes,
				ArrayDeque <LocalWatchPath> oldvalues, 
				ArrayDeque <LocalWatchPath> newvalues) {

				System.out.println ("Found " + newvalues.size() + " paths to add");

			//	while (!newpaths.isEmpty())
				//	addTreeItems (newpaths.remove().iterator(), fsRoot);
			}

    	});

    	mMain.getLocalFileModel().setOnPathsRemoved(
						new ChangeListener <ArrayDeque <LocalWatchPath> >() {

			@Override
			public void changed (
				ObservableValue<? extends ArrayDeque <LocalWatchPath> > changes,
				ArrayDeque <LocalWatchPath> oldvalues, 
				ArrayDeque<LocalWatchPath> newvalues) {
		
			System.out.println ("Found " + newvalues.size() + " paths to remove");
		
			//while (!newvalues.isEmpty())
				//removeTreeItems (newvalues.remove().iterator(), fsRoot);
			}
		});
    	
    	//initial populating of tree view
    	for (LocalWatchPath item: mMain.getLocalFileModel().getWatchedPaths()) { 
    		addTreeItem (item, fsRoot);
    	}
    }
    
    private boolean removeTreeItems (Iterator <Path> pathIt, TreeItem <String> treeItem) {
 /*   	
    	//if we've reached the end of the path, then this tree item and it's
    	//children need to be removed.
    	
    	if (!pathIt.hasNext())
    		return true;
    	
    	String pathValue = pathIt.next().toString();
    	
    	for (LocalTreeItem treeChild: treeItem.getChildren()) {
    		
    		//if the path matches, recurse
    		if (treeChild.getValue().equals (pathValue)) {
    			
    			//if the recurse returns true, the end of the path has been
    			//reached, so delete the tree item.
    			if (removeTreeItems (pathIt, treeChild))
    				treeChild.getParent().getChildren().remove(treeChild);
    				
    			return false;
    		}
    	}*/
    	return false;
    }
    
    private void addTreeItem (LocalWatchPath item, LocalTreeItem treeItem) {
    	
    	if (!item.hasNext())
    		return;
    	
    	String nextName = item.next();
    	
    	for (int x = 0; x < treeItem.getChildren().size(); x++) {
    		
    		LocalTreeItem child = treeItem.getChild(x);
    		
    		//if relative paths match, recurse and return 
    		if (child.getValue().equals(nextName)) {
    			addTreeItem (item, child);
    			return;
    		}
    	}
    	
    	//still here?  then the item found no matches against the current
    	//tree item's children.  That means we need to add the item itself
    	//as a child.  However, if the item's full path is longer, then this
    	//is not a real watch path, so it's watch path reference should be
    	//set to null
    	
    	LocalTreeItem treeChild = null;
    	
    	if (item.hasNext())
    		treeChild = new LocalTreeItem (nextName);
    	else
    		treeChild = new LocalTreeItem (item);
    	
    	treeItem.getChildren().add(treeChild);
    	addTreeItem (item, treeChild);
    }
    
    @FXML
    public void start() {
    	mMain.getLocalFileModel().startWatchService();
    }
}
