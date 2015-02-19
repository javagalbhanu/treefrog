package com.buddyware.treefrog.filesystem.view;

import java.io.IOException;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.filesystem.FileSystemType;
import com.buddyware.treefrog.util.utils;

import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FileSystemNode extends AnchorPane implements IFileSystemObject {

	@FXML private AnchorPane fs_node_title;
	@FXML private AnchorPane fs_node_image;
	
	@FXML private VBox fs_node_left;
	@FXML private VBox fs_node_right;
	
	private FileSystemType mFsType;
	private Point2D mDragPoint;
	
	private final Pane mDragContext;
	
	private FileSystemLink mDragLink;

	private final FileSystemNode self;
	
	private EventHandler <MouseEvent> mNodeDragDetected;
	private EventHandler <MouseEvent> mLinkHandleDragDetected;
	
	private EventHandler <DragEvent> mContextDragOver;
	private EventHandler <DragEvent> mContextDragDropped;
	private EventHandler <DragEvent> mContextLinkDragOver;
	private EventHandler <DragEvent> mContextLinkDragDropped;
	private EventHandler <DragEvent> mLinkHandleDragEntered;
	private EventHandler <DragEvent> mLinkHandleDragExited;
	private EventHandler <DragEvent> mLinkHandleDragDropped;
	
	public FileSystemNode(FileSystemType fs_type, FileSystemLink drag_link) {

		if (drag_link == null)
			mDragContext = null;
		else
			mDragContext = (Pane) drag_link.getParent();

		self = this;
		
		mFsType = fs_type;
		mDragLink = drag_link;
		
		loadFxml();

		setId(mFsType.toString() + Double.toString(Math.random()));
		setFileSystemNodeImage();
		
		buildNodeDragHandlers();
		
		fs_node_title.setOnDragDetected(mNodeDragDetected);

		if (drag_link != null) {

			buildLinkDragHandlers();

			fs_node_left.setOnDragEntered(mLinkHandleDragEntered);
			fs_node_right.setOnDragEntered(mLinkHandleDragEntered);
			
			fs_node_left.setOnDragExited(mLinkHandleDragExited);
			fs_node_right.setOnDragExited(mLinkHandleDragExited);
			
			fs_node_left.setOnDragDetected(mLinkHandleDragDetected);
			fs_node_right.setOnDragDetected(mLinkHandleDragDetected);
			
			fs_node_left.setOnDragDropped(mLinkHandleDragDropped);
			fs_node_right.setOnDragDropped(mLinkHandleDragDropped);			
		}
		
		fs_node_title.setOnMouseClicked(new EventHandler <MouseEvent> () {

			@Override
			public void handle(MouseEvent event) {

				final Stage stage = new Stage();
				
				FileSystemConfig controller = utils.<BorderPane, FileSystemConfig>loadFxml
									("/FileSystemConfig.fxml", stage , null);
				
				controller.setType(mFsType);
				
				/*
					BorderPane layout;
					FXMLLoader fxmlLoader = new FXMLLoader(
							getClass().getResource("/FileSystemConfig.fxml")
							);
					
					//fxmlLoader.setRoot(this); 
					//fxmlLoader.setController(this);

					try { 
						layout = fxmlLoader.load();
			        
					} catch (IOException exception) {
					    throw new RuntimeException(exception);
					}
					
					final Stage dialog = new Stage();
					dialog.initModality(Modality.WINDOW_MODAL);
					dialog.initOwner(BaseController.mMain.getPrimaryStage());
					Scene scene = new Scene (layout);
					dialog.setScene(scene);
					dialog.show();*/
			}
			
		});
		
	}
	
	@FXML
	private void initialize() {

	
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
	
	private void showFileSystemConfigDialog() {

		utils.createDialogStage("/FileSystemConfig.fxml", 
								Modality.WINDOW_MODAL, 
								BaseController.mMain.getPrimaryStage());
	}
	
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
		Point2D p2 = getParent().sceneToLocal(p);

		relocate (
				(int) (p2.getX() - mDragPoint.getX()),
				(int) (p2.getY() - mDragPoint.getY())
			);
	}
	
	public void buildNodeDragHandlers() {
		
		mContextDragOver = new EventHandler <DragEvent>() {

			//dragover to handle node dragging in the right pane view
			@Override
			public void handle(DragEvent event) {		
			
				event.acceptTransferModes(TransferMode.ANY);				
				relocateToPoint(new Point2D( event.getSceneX(), event.getSceneY()));
				event.consume();
			}
		};
		
		//dragdrop for node dragging
		mContextDragDropped = new EventHandler <DragEvent> () {
	
			@Override
			public void handle(DragEvent event) {
System.out.println("drag dropped");				
				getParent().setOnDragOver(null);
				getParent().setOnDragDropped(null);
				
				event.setDropCompleted(true);
				
				event.consume();
			}
		};
		
		//drag detection for node dragging
		mNodeDragDetected = new EventHandler <MouseEvent> () {

			@Override
			
			public void handle(MouseEvent event) {
			
				getParent().setOnDragOver(null);
				getParent().setOnDragDropped(null);
System.out.println("Drag detected");				

				getParent().setOnDragOver (mContextDragOver);
				getParent().setOnDragDropped (mContextDragDropped);
				
				mDragPoint = new Point2D(event.getX(), event.getY());

                //begin drag ops
                relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));
                
                ClipboardContent content = new ClipboardContent();
                content.putString("node_drag");

                startDragAndDrop (TransferMode.ANY).setContent(content);                
                
                event.consume();					
			}
			
		};		
	}
	
	public void bindLinkEndToLeftHandle (FileSystemLink link) {
		bindLinkEndToHandle (link, fs_node_left);
	}
	
	public void bindLinkEndToRightHandle (FileSystemLink link) {
		bindLinkEndToHandle (link, fs_node_right);
	}
	
	public void bindLinkStartToLeftHandle (FileSystemLink link) {
		bindLinkStartToHandle (link, fs_node_left);
	}
	
	public void bindLinkStartToRightHandle (FileSystemLink link) {
		bindLinkStartToHandle (link, fs_node_right);
	}	
	
	private void bindLinkEndToHandle (FileSystemLink link, VBox handle) {

		link.bindEnd (	Bindings.add(layoutXProperty(), this.sceneToLocal(handle.localToScene(0.0, 0.0)).getX()),
						Bindings.add(layoutYProperty(), this.sceneToLocal(handle.localToScene(0.0, 0.0)).getY()));
	}
	
	private void bindLinkStartToHandle (FileSystemLink link, VBox handle) {
		
		link.bindStart( Bindings.add(layoutXProperty(), fs_node_image.getWidth() / 2.0),
						Bindings.add(layoutYProperty(), fs_node_title.getHeight() + (fs_node_image.getHeight()) / 2.0));
	}

	private void buildLinkDragHandlers() {
		
		//modify handle appearance on drag entry (link creation only)
		mLinkHandleDragEntered = new EventHandler <DragEvent> () {

			@Override
			public void handle(DragEvent event) {

			}			
		};
		
		//return handle appearance to normal on drag exit
		mLinkHandleDragExited = new EventHandler <DragEvent> () {

			@Override
			public void handle(DragEvent event) {

			}
		};
		
		//drag detection for link handles
		mLinkHandleDragDetected = new EventHandler <MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				mDragContext.setOnDragOver(null);
				mDragContext.setOnDragDropped(null);
				
				mDragContext.setOnDragOver(mContextLinkDragOver);
				mDragContext.setOnDragDropped(mContextLinkDragDropped);

				VBox handle = (VBox) (event.getSource());
				
				Point2D p = handle.getParent().localToScene(
								handle.getLayoutX() + (getWidth() / 2.0),
								handle.getLayoutY() + (getHeight() / 2.0)
							);
				
				mDragLink.setControlOffsets(new Point2D(100.0, 0.0));
				
				if ((p.getX() - handle.getLayoutX()) > 0.0)
					mDragLink.controlDirectionX1().set(1.0);

				mDragLink.setStart(mDragContext.sceneToLocal(p));
				
                ClipboardContent content = new ClipboardContent();
                DragDropContainer container = new DragDropContainer ();

                container.setSource(((FileSystemNode) (handle.getParent().getParent())).getId());
                
                content.put(DragDropContainer.BindingDataFormat, container);
				
                content.putString("link_drag");
                
				handle.startDragAndDrop (TransferMode.ANY).setContent(content);	
				
				event.consume();
			}				
		};
		
		mLinkHandleDragDropped = new EventHandler <DragEvent> () {

			@Override
			public void handle(DragEvent event) {
				
				DragDropContainer value = 
						(DragDropContainer) event.getDragboard().getContent(DragDropContainer.BindingDataFormat);
				
			
				if (value == null)
					return;
				
				VBox hndl = (VBox) event.getSource();
				
				value.setTarget(((FileSystemNode) hndl.getParent().getParent()).getId());
				
				ClipboardContent content = new ClipboardContent();
				content.put(DragDropContainer.BindingDataFormat, value);
				
				event.getDragboard().clear();
				event.getDragboard().setContent(content);
				
			}
			
		};

		//drop event for link creation
		mContextLinkDragDropped = new EventHandler <DragEvent> () {

			@Override
			public void handle(DragEvent event) {
System.out.println ("Drop complete!");		
				mDragContext.setOnDragOver(null);
				mDragContext.setOnDragDropped(null);
				
				mDragLink.setVisible(false);
				
				event.setDropCompleted(true);
				event.consume();
			}
			
		};
		
		//dragover to handle dragging a link for the link creation process
		mContextLinkDragOver = new EventHandler <DragEvent> () {

			@Override
			public void handle(DragEvent event) {
System.out.println("Drag over!");				
				if (!mDragLink.isVisible())
					mDragLink.setVisible(true);
				
				event.acceptTransferModes(TransferMode.ANY);
				
				mDragLink.setEnd(event.getX(), event.getY());

				//switch the starting point's side if the ending point
				//moves past the node on the opposing side
				Point2D left_context = mDragContext.sceneToLocal(
											fs_node_left.getParent().localToScene(
													fs_node_left.getLayoutX() + (fs_node_left.getWidth() / 2.0), 
													fs_node_left.getLayoutY() + (fs_node_left.getHeight() / 2.0)						
											)
										);
				
				if (mDragLink.startX().get() == left_context.getX()) {

					Point2D right_context = mDragContext.sceneToLocal(
												fs_node_right.getParent().localToScene(
														fs_node_right.getLayoutX() + (fs_node_right.getWidth() / 2.0), 
														fs_node_right.getLayoutY() + (fs_node_right.getHeight() / 2.0)	
												)												
											);

					if (mDragLink.endX().get() > right_context.getX()) {
						mDragLink.setStart(right_context.getX(), right_context.getY());
						mDragLink.controlDirectionX1().set(1.0);
					}
				}
				else {
					if (mDragLink.endX().get() < left_context.getX()) {
						mDragLink.controlDirectionX1().set(-1.0);						
						mDragLink.setStart(left_context.getX(), left_context.getY());
					}
				}
				//if the start point is further right than the end point
				//and the control direction is leftward, then reverse direction
				//...
				//or vice-versa
				if (mDragLink.startX().get() > mDragLink.endX().get()) {
					
					if (mDragLink.controlDirectionX2().get() > 0.0)
						mDragLink.controlDirectionX2().set(1.0);
					
				}
				else if (mDragLink.controlDirectionX2().get() < 0.0)
						mDragLink.controlDirectionX2().set(-1.0);
								
				event.consume();
			}
		};		
	}

	@Override
	public IFileSystemObject getDragObject() {
		// TODO Auto-generated method stub
		return null;
	}	
}
