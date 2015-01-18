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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
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

public class FileSystemView2 extends AnchorPane {

	/**
	 * FXML initialization requirement
	 */

	@FXML private VBox fs_list;
	@FXML private SplitPane fs_split_pane;
	@FXML private AnchorPane fs_root;
	
	@FXML private AnchorPane fs_right_pane;
	@FXML private ScrollPane fs_left_pane;
	
	private EventHandler <DragEvent> mRootDragOver;
	private EventHandler <DragEvent> mRightPaneDragOver;
	private EventHandler <DragEvent> mRightPaneDragDropped;
	private EventHandler <MouseEvent> mWidgetDragDetected;
	
	private IFileSystemObject mDragObject;
	private FileSystemLink fs_link;
		
	public FileSystemView2() {
		
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource("/FileSystemView2.fxml")
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

		addFileSystemNode (FileSystemType.SOURCE_DISK);
		
		setOnDragDone (new EventHandler <DragEvent>() {

			@Override
			public void handle(DragEvent event) {				
				Dragboard db = event.getDragboard();
				System.out.println(db.getString());
				
				if (mDragObject.isVisible())
					mDragObject.setVisible(false);
				
				event.consume();
			}
		});
	}

	private void addFileSystemLink() {
		
		fs_link = new FileSystemLink();
		fs_link.setVisible(false);
		
		fs_right_pane.getChildren().add(fs_link);
	}
	
	private FileSystemNode addFileSystemNode (FileSystemType fs_type) {
		
		FileSystemNode fs_node = new FileSystemNode (fs_type, fs_right_pane, fs_link);
		
		fs_right_pane.getChildren().add(fs_node);
		
		fs_node.setLayoutX(200);
		fs_node.setLayoutY(200);
		
		return fs_node;
	}

	private void addFileSystemWidget (FileSystemType fs_type) {

		FileSystemWidget widg = 
				new FileSystemWidget (fs_type);

		widg.setOnDragDetected(mWidgetDragDetected);
		
		fs_list.getChildren().add(widg);

	}
	
	public void buildSplitPaneDragHandlers() {
		
		mWidgetDragDetected = new EventHandler <MouseEvent> () {

			@Override
			public void handle(MouseEvent event) {

				fs_right_pane.setOnDragDropped(mRightPaneDragDropped);
				fs_split_pane.setOnDragOver(mRootDragOver);
				
                //begin drag ops
                ClipboardContent content = new ClipboardContent();
                content.putString("add_node" + getId());

                
                mDragObject = ((IFileSystemObject) (event.getSource())).getDragObject();
                
                if (!fs_root.getChildren().contains((Node)mDragObject))
                	fs_root.getChildren().add((Node)mDragObject);
                
                mDragObject.relocateToPoint(new Point2D (event.getSceneX(), event.getSceneY()));
                mDragObject.startDragAndDrop (TransferMode.ANY).setContent(content);
                
                mDragObject.setVisible(true);
                
                event.consume();					
			}					
		};
		
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
		
		mRightPaneDragOver = new EventHandler <DragEvent> () {

			@Override
			public void handle(DragEvent event) {

				event.acceptTransferModes(TransferMode.ANY);
				mDragObject.relocateToPoint(fs_root.sceneToLocal(new Point2D(event.getSceneX(), event.getSceneY())));
			}
		};		
		
		mRightPaneDragDropped = new EventHandler <DragEvent> () {

			@Override
			public void handle(DragEvent event) {
				event.setDropCompleted(true);
				event.consume();
			}
		};		
	}	
}
