package com.buddyware.treefrog.view.filesystem;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
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

	/**
	 * FXML initialization requirement
	 */

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

				Dragboard db = event.getDragboard();
					
				DragContainer container = null;
				
				//check for node drag/drop
				container = (DragContainer) db.getContent(DragContainer.AddNode);
				
				if (container != null) {
					
					fs_right_pane.setOnDragOver(null);
					fs_right_pane.setOnDragDropped(null);
					fs_root.setOnDragOver(null);

					if (mDragWidget != null)
						if (mDragWidget.isVisible())
							mDragWidget.setVisible(false);
					
					FileSystem fs = addFileSystem (container.getData());	
					
					Double xCoord = Double.valueOf(
							container.getValue(FileSystemProperty.LAYOUT_X.toString()));
					
					Double yCoord = Double.valueOf(
						    container.getValue(FileSystemProperty.LAYOUT_Y.toString()));
							
					Point2D p = new Point2D( xCoord, yCoord);
					
					FileNode node = mSelf.addFileSystemNode(fs);
					
					node.setLayoutX(p.getX());
					node.setLayoutY(p.getY());
					node.setTitle(mDragWidget.getFileSystemType().toString());
				}
				
				//check for binding drag/drop
				container = (DragContainer) db.getContent(DragContainer.AddBinding);

				if (container != null) {

					String sourceId = container.getValue(SyncBindingProperty.SOURCE.toString());
					String targetId = container.getValue(SyncBindingProperty.TARGET.toString());
				
					if (sourceId != null && targetId != null) {
					
						addBindingNode (sourceId, targetId);
						addBinding(container);
					}
				}
				
				//check for node move
				container = (DragContainer) db.getContent(DragContent.MoveNode);
			
				if (container != null)
					updateFileSystem(container.getData());
				
				event.consume();
			}
		});
	}
	
	private FileSystem addFileSystem (List <Pair <String, String>> props) {
	
		FileSystem model = mFileSystems.addModel(props);
		
		mIniFile.open();
		mFileSystems.serialize(mIniFile);
		mIniFile.close();
		
		return model;
	}
	
	private void updateFileSystem (List <Pair <String, String>> props) {
		
		mFileSystems.updateModel (props);
		
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
			
				String id = n.getId();
				if (id == null)
					continue;	
				
				if (id.equals(model.getId())) {
					foundNode = true;
					break;
				}
			}
			
			if (foundNode)
				continue;

			addFileSystemNode(model);
		}
		
		if (mBindings == null)
			return;
		
		for (SyncBinding sb: mBindings.bindings()) {
			
			boolean foundBinding = false;
			
			for (Node n:fs_right_pane.getChildrenUnmodifiable()) {
				
				String id = n.getId();
				
				if (id == null)
					continue;
				
				if (id.equals(sb.getId())) {
					foundBinding = true;
					break;
				}
			}
			
			if (foundBinding)
				continue;

			addBindingNode(sb.getBindSourceId(), sb.getBindTargetId());
		}
	}
	
	private void addBindingNode (String node_one, String node_two) {
		
		FileNode sourceNode = null;
		FileNode targetNode = null;
	
		for (Node n: fs_right_pane.getChildren()) {

			if (n.getId() == null)
				continue;
			
			if (n.getId().equals(node_one))
				sourceNode = (FileNode) n;
			
			else if (n.getId().equals(node_two))
				targetNode = (FileNode) n;

			if (sourceNode != null && targetNode != null)
				break;
		}

		if (sourceNode == null || targetNode == null)
			return;

		BindingNode fsb = new BindingNode(fs_right_pane);
		
		fs_right_pane.getChildren().add(fsb);
System.out.println(sourceNode.getId() + " <--> " + targetNode.getId());
		sourceNode.bindToLink(fsb.leftStartXProperty(), fsb.leftStartYProperty());
		targetNode.bindToLink(fsb.rightStartXProperty(), fsb.rightStartYProperty());
		
		fsb.bindToNodes(sourceNode,  targetNode);
		
	}	

	private void addBinding (DragContainer container) {
		
		String source = container.getValue(SyncBindingProperty.SOURCE.toString());
		String target = container.getValue(SyncBindingProperty.TARGET.toString());
		
		mBindings.addBinding (container.getData(),
				mFileSystems.getFileSystem(source),
				mFileSystems.getFileSystem(target)
				);

		mIniFile.open();
		mBindings.serialize(mIniFile);
		mIniFile.close();		
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
		mWidgetDragDetected = new EventHandler <MouseEvent> () {

			@Override
			public void handle(MouseEvent event) {

				fs_right_pane.setOnDragDropped(null);
				fs_root.setOnDragOver(null);
				fs_right_pane.setOnDragOver(null);
				
				fs_right_pane.setOnDragDropped(mRightPaneDragDropped);
				fs_root.setOnDragOver(mRootDragOver);
				
                //begin drag ops

                mDragWidget = ((FileNodeIcon) (event.getSource())).getDragWidget();
                
                if (!fs_root.getChildren().contains((Node)mDragWidget))
                	fs_root.getChildren().add((Node)mDragWidget);
                
                mDragWidget.relocateToPoint(new Point2D (event.getSceneX(), event.getSceneY()));
                
                ClipboardContent content = new ClipboardContent();
                
                DragContainer container = new DragContainer();
                
                container.addData(FileSystemProperty.TYPE.toString(),
                		mDragWidget.getFileSystemType().toString());
                
                content.put(DragContainer.AddNode, container);
                
                mDragWidget.startDragAndDrop (TransferMode.ANY).setContent(content);
                mDragWidget.setVisible(true);
                
                event.consume();					
			}					
		};
		
		//drag over transition to move widget form left pane to right pane
		mRootDragOver = new EventHandler <DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				
				Point2D p = fs_right_pane.sceneToLocal(event.getSceneX(), event.getSceneY());

				if (!fs_right_pane.boundsInLocalProperty().get().contains(p)) {
					mDragWidget.relocateToPoint(new Point2D(event.getX(), event.getY()));
					return;
				}

				fs_root.removeEventHandler(DragEvent.DRAG_OVER, this);
				fs_right_pane.setOnDragOver(mRightPaneDragOver);
				event.consume();

			}
		};
		
		//drag over in the right pane
		mRightPaneDragOver = new EventHandler <DragEvent> () {

			@Override
			public void handle(DragEvent event) {

				event.acceptTransferModes(TransferMode.ANY);
				mDragWidget.relocateToPoint(mDragWidget.getParent().sceneToLocal(new Point2D(event.getSceneX(), event.getSceneY())));
				
				event.consume();
			}
		};		
		
		//drop action in the right pane to create a new node
		mRightPaneDragDropped = new EventHandler <DragEvent> () {

			@Override
			public void handle(DragEvent event) {

				//update the drag drop content to include the new node's location
				DragContainer container = 
						(DragContainer) event.getDragboard().getContent(DragContainer.AddNode);
				
				container.addData(FileSystemProperty.LAYOUT_X.toString(),
									Double.toString(event.getX()));
				
				container.addData(FileSystemProperty.LAYOUT_Y.toString(),
									Double.toString(event.getY()));
				
	            ClipboardContent content = new ClipboardContent();

	            content.put(DragContainer.AddNode, container);
	            
				event.getDragboard().setContent(content);
				
				//complete the drop operation and cleanup.
				event.setDropCompleted(true);
				
				event.consume();
			}
		};		
	}	
}
