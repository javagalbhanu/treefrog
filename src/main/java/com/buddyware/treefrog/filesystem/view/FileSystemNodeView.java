package com.buddyware.treefrog.filesystem.view;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.event.EventHandler;

import com.buddyware.treefrog.filesystem.FileSystemType;
import com.buddyware.treefrog.filesystem.FileSystemsModel;
import com.buddyware.treefrog.filesystem.model.FileSystemModel;
import com.buddyware.treefrog.filesystem.model.FileSystemModelProperty;
import com.buddyware.treefrog.syncbinding.model.SyncBinding;
import com.buddyware.treefrog.syncbinding.model.SyncBindingModel;
import com.buddyware.treefrog.syncbinding.model.SyncBindingProperty;
import com.buddyware.treefrog.util.IniFile;
import com.buddyware.treefrog.util.utils;

public class FileSystemNodeView extends AnchorPane {

	/**
	 * FXML initialization requirement
	 */

	@FXML private VBox fs_list;
	@FXML private SplitPane fs_split_pane;
	@FXML private AnchorPane fs_root;
	
	@FXML private Pane fs_right_pane;
	@FXML private ScrollPane fs_left_pane;
	
	private EventHandler <DragEvent> mRootDragOver;
	private EventHandler <DragEvent> mRightPaneDragOver;
	private EventHandler <DragEvent> mRightPaneDragDropped;
	private EventHandler <MouseEvent> mWidgetDragDetected;
	
	private FileSystemWidget mDragWidget;
	private FileSystemLink fs_link;
	
	private final FileSystemNodeView mSelf;
	
	private IniFile mIniFile;
	
	private FileSystemsModel mFileSystems = null;
	private SyncBindingModel mBindings = null;
	
	public FileSystemNodeView() {
		
		mSelf = this;
	    
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource("/FileSystemNodeView.fxml")
				);
		
		fxmlLoader.setRoot(this); 
		fxmlLoader.setController(this);
		
		try { 
			fxmlLoader.load();
        
		} catch (IOException exception) {
		    throw new RuntimeException(exception);
		}
		
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
		
		//add source filesystem node to right pane
		addFileSystemLink();
		
		addFileSystemWidget (FileSystemType.LOCAL_DISK);
		addFileSystemWidget (FileSystemType.AMAZON_S3);
		
		setOnDragDone (new EventHandler <DragEvent>() {

			@Override
			public void handle(DragEvent event) {

				Dragboard db = event.getDragboard();
					
				DragDropContainer container = null;
				
				//check for node drag/drop
				container = (DragDropContainer) db.getContent(DragDropContainer.AddNode);
				
				if (container != null) {
					
					fs_right_pane.setOnDragOver(null);
					fs_right_pane.setOnDragDropped(null);
					fs_root.setOnDragOver(null);

					if (mDragWidget != null)
						if (mDragWidget.isVisible())
							mDragWidget.setVisible(false);
					
					FileSystemModel fs = addFileSystem (container.getData());	
					
					Double xCoord = Double.valueOf(
							container.getValue(FileSystemModelProperty.LAYOUT_X.toString()));
					
					Double yCoord = Double.valueOf(
						    container.getValue(FileSystemModelProperty.LAYOUT_Y.toString()));
							
					Point2D p = new Point2D( xCoord, yCoord);
					
					FileSystemNode node = mSelf.addFileSystemNode(fs);
					
					node.setLayoutX(p.getX());
					node.setLayoutY(p.getY());
					node.setTitle(mDragWidget.getFileSystemType().toString());
				}
				
				//check for binding drag/drop
				container = (DragDropContainer) db.getContent(DragDropContainer.AddBinding);

				if (container != null) {

					addFileSystemBinding (
							container.getValue(SyncBindingProperty.SOURCE.toString()),
							container.getValue(SyncBindingProperty.TARGET.toString())
							);
					addBinding(container);
				}
				
				//check for node move
				container = (DragDropContainer) db.getContent(DragDropContainer.MoveNode);
			
				if (container != null)
					updateFileSystem(container.getData());
				
				event.consume();
			}
		});
	}
	
	private FileSystemModel addFileSystem (List <Pair <String, String>> props) {
	
		FileSystemModel model = mFileSystems.addModel(props);
		
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
	
	public void setFileSystemsModel (FileSystemsModel model) {

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
		for (FileSystemModel model: mFileSystems.fileSystems()) {

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

			addFileSystemBinding(sb.getBindSourceId(), sb.getBindTargetId());
		}
	}
	
	private void addFileSystemBinding(String node_one, String node_two) {
System.out.println("Binding " + node_one + " to " + node_two);		
		FileSystemNode sourceNode = null;
		FileSystemNode targetNode = null;
	
		for (Node n: fs_right_pane.getChildren()) {

			if (n.getId() == null)
				continue;
			
			if (n.getId().equals(node_one))
				sourceNode = (FileSystemNode) n;
			
			else if (n.getId().equals(node_two))
				targetNode = (FileSystemNode) n;

			if (sourceNode != null && targetNode != null)
				break;
		}

		if (sourceNode == null || targetNode == null)
			return;

		FileSystemBinding fsb = new FileSystemBinding(fs_right_pane);

		fs_right_pane.getChildren().add(fsb);

		fsb.bindLinksToNodes(sourceNode, targetNode);
	}	

	private void addBinding (DragDropContainer container) {
		
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
	
	private void addFileSystemLink() {
		
		fs_link = new FileSystemLink();
		fs_link.setVisible(false);
		
		fs_right_pane.getChildren().add(fs_link);
	}
	
	public final FileSystemNode addFileSystemNode (FileSystemModel fs) {
		
		FileSystemNode fs_node = 
				new FileSystemNode (fs.getType(), fs_link);
		
		fs_right_pane.getChildren().add(fs_node);
		
		fs_node.setLayoutX(fs.getLayoutPoint().getX());
		fs_node.setLayoutY(fs.getLayoutPoint().getY());
		fs_node.setId(fs.getId());
		fs_node.setTitle(fs.getName());
	
		return fs_node;
	}

	private void addFileSystemWidget (FileSystemType fs_type) {

		FileSystemWidget widg = 
				new FileSystemWidget (fs_type);

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

                mDragWidget = ((FileSystemWidget) (event.getSource())).getDragWidget();
                
                if (!fs_root.getChildren().contains((Node)mDragWidget))
                	fs_root.getChildren().add((Node)mDragWidget);
                
                mDragWidget.relocateToPoint(new Point2D (event.getSceneX(), event.getSceneY()));
                
                ClipboardContent content = new ClipboardContent();
                
                DragDropContainer container = new DragDropContainer();
                
                container.addData(FileSystemModelProperty.TYPE.toString(),
                		mDragWidget.getFileSystemType().toString());
                
                content.put(DragDropContainer.AddNode, container);
                
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
				DragDropContainer container = 
						(DragDropContainer) event.getDragboard().getContent(DragDropContainer.AddNode);
				
				container.addData(FileSystemModelProperty.LAYOUT_X.toString(),
									Double.toString(event.getX()));
				
				container.addData(FileSystemModelProperty.LAYOUT_Y.toString(),
									Double.toString(event.getY()));
				
	            ClipboardContent content = new ClipboardContent();

	            content.put(DragDropContainer.AddNode, container);
	            
				event.getDragboard().setContent(content);
				
				//complete the drop operation and cleanup.
				event.setDropCompleted(true);
				
				event.consume();
			}
		};		
	}	
}
