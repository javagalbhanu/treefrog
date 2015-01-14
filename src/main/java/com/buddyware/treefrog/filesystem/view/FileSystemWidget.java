package com.buddyware.treefrog.filesystem.view;

import java.io.IOException;

import com.buddyware.treefrog.filesystem.FileSystemType;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class FileSystemWidget extends AnchorPane implements IFileSystemObject{
	
	@FXML
	private AnchorPane root;
	
	@FXML
	private Label fs_container;
	
	@FXML
	private Region fs_image;
	
	private final FileSystemType mType;
	private final FileSystemWidget mDragWidget;
	private final Pane mDragContext;
	private final Pane mDragTarget;
	
	private EventHandler <DragEvent> mContextDragOver;
	private EventHandler <DragEvent> mTargetDragOver;
	private EventHandler <DragEvent> mTargetDragDropped;
	private EventHandler <DragEvent> mContextDragDone;
	
	public String getFileSystemObjectType() { return "FileSystemWidget"; }
	
	private FileSystemWidget (Pane drag_context, Pane drag_target, FileSystemType fs_type) {
		mDragContext = drag_context;
		mDragTarget = drag_target;
		mDragWidget = null;
		mType = fs_type;
		setVisible(false);
		
		loadWidget();
	}
	
	public FileSystemWidget(FileSystemType fs_type, Pane drag_context, Pane drag_target) {
		
		mType = fs_type;

		mDragContext = drag_context;
		mDragTarget = drag_target;

		mDragWidget = new FileSystemWidget (drag_context, drag_target, fs_type);
		mDragContext.getChildren().add(mDragWidget);

		buildDragHandlers();
		loadWidget();	
	}
	
	private void loadWidget() {
		
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource("/FileSystemWidget.fxml")
				);
		
		fxmlLoader.setRoot(this); 
		fxmlLoader.setController(this);

		try { 
			fxmlLoader.load();
        
		} catch (IOException exception) {
		    throw new RuntimeException(exception);
		}		
	}
	
	public FileSystemType getFileSystemType() { return mType; }
	
	@FXML
	private void initialize() {

		root.getStyleClass().add("fs-widget-background");
		
		setId(mType.toString());
		
		switch (mType) {
		
		case AMAZON_S3:
			root.getStyleClass().add("fs-widget-image");
			root.getStyleClass().add("fs-amazon-s3");
		break;
		
		case LOCAL_DISK:
			root.getStyleClass().add("fs-widget-image");			
			root.getStyleClass().add("fs-local-disk");
		break;
		
		}
		
		if (mDragWidget==null)
			return;
		
		
		setOnDragDetected( new EventHandler <MouseEvent> () {

			@Override
			public void handle(MouseEvent event) {

				mDragContext.setOnDragOver(mContextDragOver);
				mDragContext.setOnDragDone(mContextDragDone);
				mDragTarget.setOnDragOver(mTargetDragOver);
				mDragTarget.setOnDragDropped(mTargetDragDropped);
				
                //begin drag ops
                ClipboardContent content = new ClipboardContent();
                content.putString(getId());

                mDragWidget.setVisible(true);
                mDragWidget.relocateToPoint(new Point2D (event.getSceneX(), event.getSceneY()));
                mDragWidget.startDragAndDrop (TransferMode.ANY).setContent(content);
                
                //NEED TO ASSIGN DRAG WIDGET TO COMMON CHILD PRE-ADED TO DRAG CONTEXT!                                         rf                                                                             ggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg 
                event.consume();					
			}				
		});
	}
	
	public void relocateToPoint (Point2D p) {

		relocate (
				(int) (p.getX() - (getBoundsInLocal().getWidth() / 2)),
				(int) (p.getY() - (getBoundsInLocal().getHeight() / 2))
			);
	}

	public void buildDragHandlers() {

		mContextDragOver = new EventHandler <DragEvent>() {

			@Override
			public void handle(DragEvent event) {			
				mDragWidget.relocateToPoint(mDragContext.sceneToLocal( event.getSceneX(), event.getSceneY()));
				event.consume();
			}
		};
		
		mContextDragDone = new EventHandler <DragEvent>() {

			@Override
			public void handle(DragEvent event) {				
				mDragWidget.setVisible(false);
				event.consume();
			}
		};		
		
		mTargetDragOver = new EventHandler <DragEvent> () {

			@Override
			public void handle(DragEvent event) {
				event.acceptTransferModes(TransferMode.ANY);
			}
		};		
		
		mTargetDragDropped = new EventHandler <DragEvent> () {

			@Override
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				event.setDropCompleted(true);
			}
//			FileSystemNode fsn = addFileSystemNode(mDragObject.getFileSystemType());
//			fsn.relocate(e.getX(), e.getY());
		};		
	}
}