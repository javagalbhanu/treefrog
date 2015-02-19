package com.buddyware.treefrog.filesystem.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.event.EventHandler;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.filesystem.FileSystemType;
import com.buddyware.treefrog.filesystem.model.FileSystemModel;

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
	
	private IFileSystemObject mDragObject;
	private FileSystemLink fs_link;
	
	private final FileSystemNodeView mSelf;
	
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
	}
	
	@FXML
	private void initialize() {

		buildSplitPaneDragHandlers();
		
		//add source filesystem node to right pane
		addFileSystemLink();
		
		addFileSystemWidget (FileSystemType.LOCAL_DISK);
		addFileSystemWidget (FileSystemType.AMAZON_S3);

		addFileSystemNode (FileSystemType.SOURCE_DISK, new Point2D (200, 200));
		
		setOnDragDone (new EventHandler <DragEvent>() {

			@Override
			public void handle(DragEvent event) {

				Dragboard db = event.getDragboard();
				
				DragDropContainer container = (DragDropContainer) db.getContent(DragDropContainer.BindingDataFormat);
				
				if (container != null)
					addFileSystemBinding (container);
				
				if (mDragObject != null)
					if (mDragObject.isVisible())
						mDragObject.setVisible(false);

				event.consume();
			}
		});
		
		constructFileSystemView();
	}
	
	private void constructFileSystemView () {
		/*
		for (FileSystemModel model: BaseController.mMain.fileSystems().values()) {
			
			addFileSystemNode (model.getType(), model.getLayoutPoint());
			
		}*/
	}
	
	private void addFileSystemBinding(DragDropContainer container) {
		
		FileSystemNode sourceNode = null;
		FileSystemNode targetNode = null;
		
		for (Node n: fs_right_pane.getChildren()) {

			if (n.getId() == null)
				continue;
			
			if (n.getId().equals(container.getSource()))
				sourceNode = (FileSystemNode) n;
			else if (n.getId().equals(container.getTarget()))
				targetNode = (FileSystemNode) n;

			if (sourceNode != null && targetNode != null)
				break;
		}
		
		if (sourceNode == null || targetNode == null)
			return;
		
		FileSystemBinding fsb = new FileSystemBinding(fs_right_pane);

		fs_right_pane.getChildren().add(fsb);

		fsb.bindLeftLinkToNode(sourceNode);
		fsb.bindRightLinkToNode(targetNode);

	}

	private void addFileSystemLink() {
		
		fs_link = new FileSystemLink();
		fs_link.setVisible(false);
		
		fs_right_pane.getChildren().add(fs_link);
	}
	
	public final FileSystemNode addFileSystemNode (FileSystemType fs_type, Point2D location) {
		
		FileSystemNode fs_node = new FileSystemNode (fs_type, fs_link);
		
		fs_right_pane.getChildren().add(fs_node);
		
		fs_node.setLayoutX(location.getX());
		fs_node.setLayoutY(location.getY());
		
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

                mDragObject = ((IFileSystemObject) (event.getSource())).getDragObject();
                
                if (!fs_root.getChildren().contains((Node)mDragObject))
                	fs_root.getChildren().add((Node)mDragObject);
                
                mDragObject.relocateToPoint(new Point2D (event.getSceneX(), event.getSceneY()));
                
                ClipboardContent content = new ClipboardContent();
                content.putString(mDragObject.getFileSystemType().toString());

                mDragObject.startDragAndDrop (TransferMode.ANY).setContent(content);
                mDragObject.setVisible(true);
                
                event.consume();					
			}					
		};
		
		//drag over transition to move widget form left pane to right pane
		mRootDragOver = new EventHandler <DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				
				Point2D p = fs_right_pane.sceneToLocal(event.getSceneX(), event.getSceneY());

				if (!fs_right_pane.boundsInLocalProperty().get().contains(p)) {
					mDragObject.relocateToPoint(new Point2D(event.getX(), event.getY()));
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
				mDragObject.relocateToPoint(mDragObject.getParent().sceneToLocal(new Point2D(event.getSceneX(), event.getSceneY())));
				
				event.consume();
			}
		};		
		
		//drop action in the right pane to create a new node
		mRightPaneDragDropped = new EventHandler <DragEvent> () {

			@Override
			public void handle(DragEvent event) {
				Point2D p = fs_right_pane.sceneToLocal(new Point2D (event.getSceneX(), event.getSceneY()));	
				
				mSelf.addFileSystemNode(mDragObject.getFileSystemType(), p);
				event.setDropCompleted(true);

				fs_right_pane.setOnDragOver(null);
				fs_right_pane.setOnDragDropped(null);
				fs_root.setOnDragOver(null);
				
				event.consume();
			}
		};		
	}	
}
