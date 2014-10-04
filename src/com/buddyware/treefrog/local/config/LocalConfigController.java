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

				while (!newvalues.isEmpty()) {
System.out.println ("Adding " + newvalues.peek());					
					addTreeItem (newvalues.remove(), fsRoot);
				}
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
    
    private void addTreeItem (LocalWatchPath pathItem, LocalTreeItem treeItem) {

    	/*
    	 * addTreeItem (LocalWatchPath item, LocalTreeItem treeItem)
    	 * 
    	 * pathItem - 	an item representing an element to be added 
    	 * 				to the tree view
    	 * treeItem - 	an existing element of the tree view which is an 
    	 * 				ancestor of the pathItem
    	 * 
    	 * Method recurses the tree view's items with each pathItem passed.
    	 * Recursion stops when the current treeItem is no longer an ancestor
    	 * of the pathItem.  At this point, it is added to the treeview. 
    	 */

    	//at the first level, incoming paths need to be converted to relative
    	//paths against their proper ancestor

    	if (!pathItem.hasNext()) { 		
    		return;
    	}
    	
    	boolean isRoot = (treeItem == fsRoot);
    	
    	String pathName = "";
    	
    	if (!isRoot)
    		pathName = pathItem.next();

    	//Recursion occurs if the pathItem is a descendant of (or is exactly)
		//the tree item's path.
    	for (int x = 0; x < treeItem.getChildCount(); x++) {

    		LocalTreeItem treeChild = treeItem.getChild(x);
    		LocalWatchPath childPath = pathItem;
    		
    		//below root level, continue if the pathName doesn't match
    		//the treeitme's values
    		if (!isRoot) {
    			if (!treeChild.getValue().equals(pathName))
    				continue;
    		}
    		//otherwise, at root level, convert the pathItem to a descendant
    		//of the treeItem's path
    		else {
				childPath = treeChild.getAsDescendant(pathItem);
    		}
    		
			//if still here, it's a proper child path.
    		if (childPath != null) {
    			addTreeItem (childPath, treeChild);
    			return;
    		}
    	}
    	
    	/*
    	 * Still here?  Add the pathItem itself as a child.  
    	 * However, if the pathItem still has more names to parse,
    	 * then this is not a watch path "destination", so it's watch path 
    	 * reference should be set to null by assigning only a string value 
    	 * to the tree item.
    	 * 
    	 * This efficiency works because the Java FileVisitor parses depth-first
    	 * ensuring that ancestors will always occur the list before their
    	 * descendants.  Only the order of siblings cannot be guaranteed.
    	 */
    	
    	LocalTreeItem treeChild = null;
    	LocalWatchPath pathChild = pathItem;

    	if (isRoot) {
    		pathChild = pathItem.relativizeToParent();
    		pathChild.next();
    		treeChild = new LocalTreeItem (pathItem);
    	}
    	else { 

    	if (pathItem.hasNext())
    		treeChild = new LocalTreeItem (pathName);
    	else
    		treeChild = new LocalTreeItem (pathItem);
    	}
    	
    	treeItem.getChildren().add(treeChild);
    	
    	addTreeItem (pathChild, treeChild);
    }
    
    @FXML
    public void start() {
    	mMain.getLocalFileModel().startWatchService();
    }
}
