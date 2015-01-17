package com.buddyware.treefrog.filesystem.view;

import java.io.IOException;

import com.buddyware.treefrog.filesystem.FileSystemType;

import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.transform.Transform;

public class FileSystemNode extends AnchorPane implements IFileSystemObject{

	@FXML private AnchorPane fs_node_title;
	@FXML private AnchorPane fs_node_image;
	
	@FXML private VBox fs_node_left;
	@FXML private VBox fs_node_right;
	
	@FXML private Circle fs_left_link_handle;
	@FXML private Circle fs_right_link_handle;

	
	private FileSystemType mFsType;
	private Point2D mDragPoint;
	
	private final Pane mDragContext;
	private FileSystemLink mDragLink;

	private EventHandler <MouseEvent> mNodeDragDetected;
	private EventHandler <DragEvent> mContextDragOver;
	private EventHandler <DragEvent> mContextDragDropped;
	private EventHandler <DragEvent> mContextDragDone;
	private EventHandler <DragEvent> mContextLinkDragOver;
	private EventHandler <DragEvent> mContextLinkDragDropped;
	
	private Transform mHandleTransform;
	
	public FileSystemNode(FileSystemType fs_type, Pane drag_context, FileSystemLink drag_link) {
		
		mDragContext = drag_context;
		mFsType = fs_type;
		mDragLink = drag_link;
		
		loadFxml();

		setId(fs_type.toString() + Double.toString(Math.random()));
		setFileSystemNodeImage();
		
		buildDragHandlers();

		fs_node_title.setOnDragDetected(mNodeDragDetected);
		fs_node_image.setOnDragDetected(mNodeDragDetected);
				
		fs_left_link_handle.setOnDragDetected(new EventHandler <MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

					mDragContext.setOnDragOver(mContextLinkDragOver);
					mDragContext.setOnDragDone(mContextDragDone);
					mDragContext.setOnDragDropped(mContextLinkDragDropped);
					
					mDragLink.setControlOffsets(new Point2D(-100.0, 0.0));

					Point2D p = fs_left_link_handle.localToScene(
									fs_left_link_handle.centerXProperty().get(),
									fs_left_link_handle.centerYProperty().get()
								);
					
					mDragLink.setStart(mDragContext.sceneToLocal(p));
					
	                ClipboardContent content = new ClipboardContent();
	                content.putString(getId());
					
					fs_left_link_handle.startDragAndDrop (TransferMode.ANY).setContent(content);	
					
					mDragLink.setVisible(true);

			}
		});

		fs_right_link_handle.setOnDragDetected(new EventHandler <MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				
				mDragContext.setOnDragOver(mContextLinkDragOver);
				mDragContext.setOnDragDone(mContextDragDone);
				mDragContext.setOnDragDropped(mContextLinkDragDropped);
				
				mDragLink.setControlOffsets(new Point2D(-100.0, 0.0));

				Point2D p = fs_left_link_handle.localToScene(
								fs_left_link_handle.centerXProperty().get(),
								fs_left_link_handle.centerYProperty().get()
							);
				
				mDragLink.setStart(mDragContext.sceneToLocal(p));
				
                ClipboardContent content = new ClipboardContent();
                content.putString(getId());
				
				fs_left_link_handle.startDragAndDrop (TransferMode.ANY).setContent(content);	
				
				mDragLink.setVisible(true);

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

		//drag detection for node dragging
		mNodeDragDetected = new EventHandler <MouseEvent> () {

			@Override
			public void handle(MouseEvent event) {

				mDragContext.setOnDragOver(mContextDragOver);
				mDragContext.setOnDragDone(mContextDragDone);
				mDragContext.setOnDragDropped(mContextDragDropped);
				
				mDragPoint = new Point2D(event.getX(), event.getY());
				
                //begin drag ops
                ClipboardContent content = new ClipboardContent();
                content.putString("node_drag" + getId());

                relocateToPoint(mDragPoint);
                startDragAndDrop (TransferMode.ANY).setContent(content);
                
                event.consume();					
			}
			
		};

		//drop event for link creation
		mContextLinkDragDropped = new EventHandler <DragEvent> () {

			@Override
			public void handle(DragEvent event) {
				mDragLink.setVisible(false);
			}
			
		};
		
		//dragover to handle dragging a link for the link creation process
		mContextLinkDragOver = new EventHandler <DragEvent> () {

			@Override
			public void handle(DragEvent event) {	
			
				event.acceptTransferModes(TransferMode.ANY);				

				mDragLink.setEnd(event.getX(), event.getY());

				//switch the starting point's side if the ending point
				//moves past the node on the opposing side
				Point2D left_scene = fs_left_link_handle.localToScene(fs_left_link_handle.centerXProperty().get(), fs_left_link_handle.centerYProperty().get());
				Point2D left_context = mDragContext.sceneToLocal(left_scene);
				
				if (mDragLink.startX().get() == left_context.getX()) {

					Point2D right_scene = fs_right_link_handle.localToScene(fs_right_link_handle.centerXProperty().get(), fs_right_link_handle.centerYProperty().get());
					Point2D right_context = mDragContext.sceneToLocal(right_scene);

					if (mDragLink.endX().get() > right_context.getX()) {
						mDragLink.setStart(right_context.getX(), right_context.getY());
						mDragLink.controlDirectionX1().set(-1.0);
					}
				}
				else {
					if (mDragLink.endX().get() < left_context.getX()) {
						mDragLink.controlDirectionX1().set(1.0);						
						mDragLink.setStart(left_context.getX(), left_context.getY());
					}
				}
				//if the start point is further right than the end point
				//and the control direction is leftward, then reverse direction
				//...
				//or vice-versa
				if (mDragLink.startX().get() > mDragLink.endX().get()) {
					
					if (mDragLink.controlDirectionX2().get() > 0.0)
						mDragLink.controlDirectionX2().set(-1.0);
					
				}
				else if (mDragLink.controlDirectionX2().get() < 0.0)
						mDragLink.controlDirectionX2().set(1.0);
								
				event.consume();
			}
		};
		
		mContextDragOver = new EventHandler <DragEvent>() {

			//dragover to handle node dragging in the right pane view
			@Override
			public void handle(DragEvent event) {
								
				relocateToPoint(new Point2D( event.getSceneX(), event.getSceneY()));
				event.consume();
			}
		};
		
		//dragdrop for node dragging
		mContextDragDropped = new EventHandler <DragEvent> () {
	
			@Override
			public void handle(DragEvent event) {
				event.setDropCompleted(true);
			}
		};		
	}

	@Override
	public IFileSystemObject getDragObject() {
		// TODO Auto-generated method stub
		return null;
	}	
}
