package com.buddyware.treefrog.filesystem.view;

import java.io.IOException;

import com.buddyware.treefrog.filesystem.FileSystemType;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class FileSystemNode extends AnchorPane implements IFileSystemObject{

	@FXML private AnchorPane fs_node_image;
	@FXML private VBox fs_node_left;
	@FXML private VBox fs_node_right;
	
	private FileSystemType mFsType;
	private Point2D mDragPoint;
	
	private final Pane mDragContext;
	
	private EventHandler <DragEvent> mContextDragOver;
	private EventHandler <DragEvent> mContextDragDropped;
	private EventHandler <DragEvent> mContextDragDone;
		
	public FileSystemNode(FileSystemType fs_type, Pane drag_context) {
		
		mDragContext = drag_context;
		mFsType = fs_type;
		
		loadFxml();

		setId(fs_type.toString() + Double.toString(Math.random()));
		setFileSystemNodeImage();
		
		buildDragHandlers();
		
		setOnDragDetected(
			new EventHandler <MouseEvent> () {

				@Override
				public void handle(MouseEvent event) {
				
					mDragContext.setOnDragOver(mContextDragOver);
					mDragContext.setOnDragDone(mContextDragDone);
					mDragContext.setOnDragDropped(mContextDragDropped);
					
					mDragPoint = sceneToLocal(new Point2D(event.getX(), event.getY()));
					
	                //begin drag ops
	                ClipboardContent content = new ClipboardContent();
	                content.putString(getId());

	                relocateToPoint(mDragPoint);
	                startDragAndDrop (TransferMode.ANY).setContent(content);
	                
	                event.consume();					
				}
				
			});		
	}
		
	private void loadFxml() {
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource("/FileSystemNode.fxml")
				);
		
		fxmlLoader.setRoot(this); 
		fxmlLoader.setController(this);
		
		try { 
			fxmlLoader.load();
        
		} catch (IOException exception) {
		    throw new RuntimeException(exception);
		}		
	}
	
	public String getFileSystemObjectType() { return "FileSystemNode"; }
	
	public FileSystemType getFileSystemType() { return mFsType; }
	
	private void setFileSystemNodeImage() {
		
		switch (mFsType) {
		
		case SOURCE_DISK:
			
			fs_node_image.getStyleClass().add("fs-source-disk");
			
		break;
		
		case LOCAL_DISK:
			
			fs_node_image.getStyleClass().add("fs-local-disk");
			
		break;
		
		case AMAZON_S3:
			
			fs_node_image.getStyleClass().add("fs-amazon-s3");
			
		break;
		
		default:
		break;
		}
	}
	
	public void relocateToPoint (Point2D p) {
		Point2D p2 = this.getParent().sceneToLocal(p);

		relocate (
				(int) (p2.getX() - mDragPoint.getX()),
				(int) (p2.getY() - mDragPoint.getY())
			);
	}
	
	public void buildDragHandlers() {

		mContextDragOver = new EventHandler <DragEvent>() {

			@Override
			public void handle(DragEvent event) {	
System.out.println("OnDragOver");				
				event.acceptTransferModes(TransferMode.ANY);				
				relocateToPoint(mDragContext.sceneToLocal( event.getSceneX(), event.getSceneY()));
				event.consume();
			}
		};
		
		mContextDragDone = new EventHandler <DragEvent>() {

			@Override
			public void handle(DragEvent event) {				
				event.consume();
			}
		};		
	
		
		mContextDragDropped = new EventHandler <DragEvent> () {

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
