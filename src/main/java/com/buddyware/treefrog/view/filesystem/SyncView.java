package com.buddyware.treefrog.view.filesystem;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;

import com.buddyware.treefrog.utils;
import com.buddyware.treefrog.model.IniFile;
import com.buddyware.treefrog.model.filesystem.FileSystem;
import com.buddyware.treefrog.model.filesystem.FileSystemModel;
import com.buddyware.treefrog.model.filesystem.FileSystemProperty;
import com.buddyware.treefrog.model.filesystem.FileSystemType;
import com.buddyware.treefrog.model.syncbinding.SyncBinding;
import com.buddyware.treefrog.model.syncbinding.SyncBindingModel;
import com.buddyware.treefrog.model.syncbinding.SyncBindingProperty;
import com.buddyware.treefrog.view.CustomFxml;

public class SyncView extends AnchorPane {

	@FXML private VBox fs_list;
	@FXML private SplitPane fs_split_pane;
	@FXML private AnchorPane fs_root;
	
	@FXML private Pane fs_right_pane;
	@FXML private ScrollPane fs_left_pane;
	
	private static final URL mSyncViewPath = 
			SyncView.class.getResource("/SyncView.fxml");
	
	private EventHandler <DragEvent> mRootDragOver;
	private EventHandler <DragEvent> mRightPaneDragOver;
	private EventHandler <DragEvent> mRightPaneDragDropped;
	private EventHandler <MouseEvent> mWidgetDragDetected;
	
	private FileNodeIcon mDragWidget;
	
	private final SyncView mSelf;
	
	private IniFile mIniFile;
	
	private FileSystemModel mFileSystems = null;
	private SyncBindingModel mBindings = null;
	
	public SyncView() {
		
		mSelf = this;
	    
		CustomFxml.load (mSyncViewPath, this);
		
		try {
			mIniFile = 	new IniFile
					(utils.getApplicationDataPath() + "/filesystems.ini");		

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	@FXML
	private void initialize() {

		buildSplitPaneDragHandlers();
		
		addFileSystemWidget (FileSystemType.LOCAL_DISK);
		addFileSystemWidget (FileSystemType.AMAZON_S3);
		
		setOnDragDone (new EventHandler <DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				
				fs_right_pane.setOnDragOver(null);
				fs_right_pane.setOnDragDropped(null);
				fs_root.setOnDragOver(null);

				if (mDragWidget != null)
					mDragWidget.setVisible(false);
				
				DragContainer container = DragContent.get(event,  DragContent.AddNode);
				
				if (container != null)
					addFileSystem (container);
				
				container = DragContent.get(event, DragContent.AddBinding);

				if (container != null)
					addBinding(container);

				container = DragContent.get (event, DragContent.MoveNode);
			
				if (container != null)
					updateFileSystem(container);
				
				event.consume();
			}
		});
	}
	
	private FileSystem addFileSystem (DragContainer container) {
	
		FileSystem model = mFileSystems.addModel(container.getData());
		
		mIniFile.open();
		mFileSystems.serialize(mIniFile);
		mIniFile.close();
		
		addFileSystemNode (model);
		
		return model;
	}
	
	private void updateFileSystem (DragContainer container) {
		
		mFileSystems.updateModel (container.getData());
		
		mIniFile.open();
		mFileSystems.serialize(mIniFile);
		mIniFile.close();
		
	}
	
	public void setFileSystemsModel (FileSystemModel model) {

		mFileSystems = model;
		mFileSystems.deserialize(mIniFile);
		refresh();
	}
	
	public void setBindingsModel (SyncBindingModel model) {

		mBindings = model;
		mBindings.deserialize(mIniFile, mFileSystems);
		refresh();
		
	}

	private void refresh() {

		//iterate the models and bindings, adding any that are missing
		for (FileSystem model: mFileSystems.fileSystems()) {

			boolean foundNode = false;
			
			for (Node n:fs_right_pane.getChildrenUnmodifiable()) {
			
				if (n.getId() == null)
					continue;	
				
				foundNode = n.getId().equals(model.getId());
					
				if (foundNode)
					break;
			}
			
			if (!foundNode)
				addFileSystemNode(model);
		}
		
		if (mBindings == null)
			return;
		
		for (SyncBinding sb: mBindings.bindings()) {
			
			boolean foundBinding = false;
			
			for (Node n:fs_right_pane.getChildrenUnmodifiable()) {
				
				if (n.getId() == null)
					continue;
				
				foundBinding = n.getId().equals(sb.getId());

				if (foundBinding)
					break;
			}
			
			if (!foundBinding)
				addBindingNode(sb.getBindSourceId(), sb.getBindTargetId());
		}
	}

	private void addBinding (DragContainer container) {
		
		String source = container.getValue(SyncBindingProperty.SOURCE.toString());
		String target = container.getValue(SyncBindingProperty.TARGET.toString());
		
		if (source == null || target == null)
			return;
		
		mBindings.addBinding (container.getData(),
				mFileSystems.getFileSystem(source),
				mFileSystems.getFileSystem(target)
				);

		mIniFile.open();
		mBindings.serialize(mIniFile);
		mIniFile.close();
		
		addBindingNode (source, target);
	}
	
	private void addBindingNode (String node_one, String node_two) {
		
		FileNode sourceNode = null;
		FileNode targetNode = null;
		boolean foundNodes = false;
		
		for (Node n: fs_right_pane.getChildren()) {

			if (n.getId() == null)
				continue;
			
			if (n.getId().equals(node_one))
				sourceNode = (FileNode) n;
			
			else if (n.getId().equals(node_two))
				targetNode = (FileNode) n;

			foundNodes = (sourceNode != null && targetNode != null);
					
			if (foundNodes)
				break;
		}

		if (!foundNodes)
			return;

		BindingNode fsb = new BindingNode(fs_right_pane);
		
		fs_right_pane.getChildren().add(fsb);

		sourceNode.bindToLink(fsb.leftStartXProperty(), fsb.leftStartYProperty());
		targetNode.bindToLink(fsb.rightStartXProperty(), fsb.rightStartYProperty());
		
		fsb.bindToNodes(sourceNode,  targetNode);
		
	}		
	
	public final FileNode addFileSystemNode (FileSystem fs) {
		
		FileNode fs_node = 
				new FileNode (fs.getType(), fs);
		
		fs_right_pane.getChildren().add(fs_node);
		
		fs_node.setLayoutX(fs.getLayoutPoint().getX());
		fs_node.setLayoutY(fs.getLayoutPoint().getY());
		fs_node.setId(fs.getId());
		fs_node.setTitle(fs.getName());
		fs_node.addModelUpdateListener(new InvalidationListener () {

			@Override
			public void invalidated(Observable observable) {;				
				mIniFile.open();
				mFileSystems.serialize(mIniFile);
				mIniFile.close();
			}
			
		});
		
		return fs_node;
	}

	private void addFileSystemWidget (FileSystemType fs_type) {

		FileNodeIcon widg = 
				new FileNodeIcon (fs_type);

		widg.setOnDragDetected(mWidgetDragDetected);
		
		fs_list.getChildren().add(widg);

	}
	
	public void buildSplitPaneDragHandlers() {
		
		//drag detection for widget in the left-hand scroll pane to create a node in the right pane 
		mWidgetDragDetected = (MouseEvent e) -> {

			fs_right_pane.setOnDragDropped(null);
			fs_root.setOnDragOver(null);
			fs_right_pane.setOnDragOver(null);
			
			fs_right_pane.setOnDragDropped(mRightPaneDragDropped);
			fs_root.setOnDragOver(mRootDragOver);
			
            //begin drag ops

            mDragWidget = ((FileNodeIcon) (e.getSource())).getDragWidget();
            
            if (!fs_root.getChildren().contains((Node) mDragWidget))
            	fs_root.getChildren().add((Node)mDragWidget);
            
            mDragWidget.relocateToPoint(new Point2D (e.getSceneX(), e.getSceneY()));
            
            mDragWidget.startDragAndDrop (TransferMode.ANY).setContent(
                    DragContent.create (DragContent.AddNode, 
            				FileSystemProperty.TYPE.toString(),
            				mDragWidget.getFileSystemType().toString()
            		)
            );
                        
            mDragWidget.setVisible(true);
            
            e.consume();					
		};
		
		//drag over transition to move widget form left pane to right pane
		mRootDragOver = (DragEvent e) -> {

			Point2D p = fs_right_pane.sceneToLocal(e.getSceneX(), e.getSceneY());

			if (!fs_right_pane.boundsInLocalProperty().get().contains(p)) {
				mDragWidget.relocateToPoint(new Point2D(e.getX(), e.getY()));
				return;
			}

			fs_root.removeEventHandler(DragEvent.DRAG_OVER, mRootDragOver);
			fs_right_pane.setOnDragOver(mRightPaneDragOver);
			e.consume();
		};
		
		//drag over in the right pane
		mRightPaneDragOver = (DragEvent e) -> {
			
			e.acceptTransferModes(TransferMode.ANY);
			mDragWidget.relocateToPoint(
					mDragWidget.getParent().sceneToLocal(
							new Point2D(e.getSceneX(), e.getSceneY())
					)
			);
			
			e.consume();
		};		
		
		//drop action in the right pane to create a new node
		mRightPaneDragDropped = (DragEvent e) -> {

			//update the drag drop content to include the new node's location
			DragContent.add(e, DragContent.AddNode,
							FileSystemProperty.LAYOUT_X.toString(),
							Double.toString(e.getX())
							);
			
			DragContent.add(e, DragContent.AddNode,
					FileSystemProperty.LAYOUT_Y.toString(),
					Double.toString(e.getY())
					);
			
			//complete the drop operation and cleanup.
			e.setDropCompleted(true);
			e.consume();
		};		
	}	
}
