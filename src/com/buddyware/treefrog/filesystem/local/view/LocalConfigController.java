package com.buddyware.treefrog.filesystem.local.view;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.filesystem.local.model.LocalWatchPath;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeView;

public class LocalConfigController extends BaseController {

	@FXML
	private TreeView fileTree;
	
	@FXML
	private ListView fileList;
	
	private LocalTreeItem fsRoot =
			new LocalTreeItem (mMain.getLocalFileModel().toString());
	
    /**
     * FXML initialization requirement
     */
    @FXML
    private void initialize() {
    	
    	fileTree.setRoot(fsRoot);
    	
    	mMain.getLocalFileModel().setOnPathsAdded(new ListChangeListener <String>(){

			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends String> arg0) {
				
				System.out.println ("Found " + arg0.getList().size() + " paths to add");

				for (String path: arg0.getList())	
					updateTree (new LocalWatchPath (path), fsRoot, false);
			}
    		
    	});
    	
    	mMain.getLocalFileModel().setOnPathsRemoved(new ListChangeListener <String>(){

			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends String> arg0) {
				
				System.out.println ("Found " + arg0.getList().size() + " paths to remove");

				for (String path: arg0.getList())
					updateTree (new LocalWatchPath (path), fsRoot, true);			
			}
    		
    	});    	
    	mMain.getLocalFileModel().start();
    }
    
    private void updateTree (LocalWatchPath pathItem, LocalTreeItem treeItem,
    		boolean doRemoval) 
    {

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

    	//Recursion occurs if the pathItem is a descendant of (or is exactly)
		//the tree item's path.
    	for (int x = 0; x < treeItem.getChildCount(); x++) {

    		LocalTreeItem treeChild = treeItem.getChild(x);
    		
    		//skip loop if this child is not an ancestor of the pathitem
    		if (!treeChild.getPath().isAncestorOf(pathItem))
    			continue;
    		
    		//if is an ancestor, it may also be the path itself.
    		//if we're removing and the tree child is the path item,
    		//remove the tree child.
    		if (doRemoval) {
    			if (treeChild.getPath().equals(pathItem)) {
    				treeItem.getChildren().remove(x);
    				return;
    			}
    		}
    		
    		updateTree (pathItem, treeChild, doRemoval);
    		return;

    	}
    	
    	/* still here? Then item was not found.  If adding items,
    	 * add to treeitem's children.  If removing, then abort.
    	 */
    	
    	if (doRemoval)
    		return;
    	
		treeItem.getChildren().add (new LocalTreeItem (pathItem));
    }
    
    @FXML
    public void start() {
    	
    }
}
