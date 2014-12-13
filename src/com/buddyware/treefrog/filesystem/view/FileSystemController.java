package com.buddyware.treefrog.filesystem.view;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.filesystem.model.SyncPath;
import com.buddyware.treefrog.filesystem.model.local.LocalWatchPath;
import com.buddyware.treefrog.util.utils;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;


public class FileSystemController extends BaseController {

	private final static String TAG = "FileSystemController";
	private int mPathsAdded = 0;
	@FXML
	private TreeView fileTree;
	
	@FXML
	private ListView fileList;
	
	@FXML
	private StackPane paneWizardContent;
	
	@FXML
	private Button addNewFileSystem;
	
	private FileSystemTreeItem fsRoot =
			new FileSystemTreeItem (mMain.getLocalFileModel().toString());
	
    /**
     * FXML initialization requirement
     */
    @FXML
    private void initialize() {
    	
System.out.println ("Creating FileSystemController...");

    	fileTree.setRoot(fsRoot);
    	
    	mMain.getLocalFileModel().setOnPathsChanged(new ListChangeListener <SyncPath>(){

			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends SyncPath> arg0) {
				
				System.out.println ("Found " + arg0.getList().size() + " paths to add");

				for (SyncPath path: arg0.getList())	{
					
					//only update directories
					if (path.getFile()==null)
						updateTree (new LocalWatchPath (path.getPath()), fsRoot, false);
				}
				System.out.println (mPathsAdded + " path added");

			}
    		
    	});
    	    	
    	mMain.startFileSystems();
    	this.parentStage = mMain.getPrimaryStage();
    }
    
    private void updateTree (LocalWatchPath pathItem, FileSystemTreeItem treeItem,
    		boolean doRemoval) 
    {

    	/*
    	 * addTreeItem (LocalWatchPath item, FileSystemTreeItem treeItem)
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

    		FileSystemTreeItem treeChild = treeItem.getChild(x);
    		
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

		treeItem.getChildren().add (new FileSystemTreeItem (pathItem));
    }
    
    @FXML
    public void createNewFileSystem() {

    	System.out.println (this.parentStage == null);
    	utils.<AnchorPane>createDialogStage("filesystem/view/AddFileSystem.fxml", Modality.WINDOW_MODAL, this.parentStage).show();

    }
}
