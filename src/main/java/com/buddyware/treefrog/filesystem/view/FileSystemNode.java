package com.buddyware.treefrog.filesystem.view;

import java.io.IOException;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.filesystem.FileSystemType;
import com.buddyware.treefrog.filesystem.model.FileSystem;
import com.buddyware.treefrog.filesystem.model.FileSystemProperty;
import com.buddyware.treefrog.syncbinding.model.SyncBindingProperty;
import com.buddyware.treefrog.util.utils;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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

public class FileSystemNode extends AnchorPane {

	@FXML private AnchorPane fs_node_title;
	@FXML private AnchorPane fs_node_image;
	@FXML private Label fs_node_title_bar;
	
	@FXML private VBox fs_node_left;
	@FXML private VBox fs_node_right;
	
	private InvalidationListener mModelUpdateListener = null;
	
	private FileSystemType mFsType;
	private Point2D mDragPoint;
	
	private final Pane mDragContext;
	
	private FileSystemLink mDragLink;
	
	private FileSystem mModel;
	
	private EventHandler <MouseEvent> mNodeDragDetected;
	private EventHandler <MouseEvent> mLinkHandleDragDetected;
	
	private EventHandler <DragEvent> mContextDragOver;
	private EventHandler <DragEvent> mContextDragDropped;
	private EventHandler <DragEvent> mContextLinkDragOver;
	private EventHandler <DragEvent> mContextLinkDragDropped;

	private EventHandler <DragEvent> mLinkHandleDragDropped;
	
	private final FileSystemNode mSelf;
	
	public FileSystemNode(FileSystemType fs_type, FileSystemLink drag_link, FileSystem fs) {

		if (drag_link == null)
			mDragContext = null;
		else
			mDragContext = (Pane) drag_link.getParent();
		
		mFsType = fs_type;
		mDragLink = drag_link;
		mModel = fs;
		
		loadFxml();

		setId(mFsType.toString());
		setFileSystemNodeImage();
		
		buildNodeDragHandlers();
		
		fs_node_title.setOnDragDetected(mNodeDragDetected);
		fs_node_title_bar.setText(mFsType.toString());
		
		
		if (drag_link != null) {

			buildLinkDragHandlers();
			
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
				controller.setModel (mModel);
				controller.addModelUpdateListener(mModelUpdateListener);
			}
			
		});
		
		mSelf = this;
		
	}
	
	@FXML
	private void initialize() {

	
	}

	public void addModelUpdateListener (InvalidationListener listener) {
		mModelUpdateListener = listener;
	}
	
	public void setTitle(String text) { fs_node_title_bar.setText(text); }
	
	public String getTitle() { return fs_node_title_bar.getText(); }
	
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
			
				getParent().setOnDragOver(null);
				getParent().setOnDragDropped(null);
				
				DragDropContainer container = 
					    (DragDropContainer) event.getDragboard().getContent(DragDropContainer.MoveNode);
												
					if (container == null)
					    return;
					
				ClipboardContent content = new ClipboardContent();
									
				container.addData(  FileSystemProperty.LAYOUT_X.toString(),
									Double.toString(mSelf.getLayoutX()));

				container.addData(  FileSystemProperty.LAYOUT_Y.toString(),
									Double.toString(mSelf.getLayoutY()));

				content.put(DragDropContainer.MoveNode, container);
								
				event.getDragboard().setContent(content);
				
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

				getParent().setOnDragOver (mContextDragOver);
				getParent().setOnDragDropped (mContextDragDropped);
				
				mDragPoint = new Point2D(event.getX(), event.getY());

                //begin drag ops
                relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));
                
                ClipboardContent content = new ClipboardContent();
                
                DragDropContainer container = new DragDropContainer();
                
				Node evtSource = (Node) event.getSource();
				
				FileSystemNode fsNode = 
						(FileSystemNode) evtSource.getParent().getParent().getParent();
				
				container.addData(  FileSystemProperty.ID.toString(),
									fsNode.getId());
				
                content.put(DragDropContainer.MoveNode, container);
                
                startDragAndDrop (TransferMode.ANY).setContent(content);
                
                event.consume();					
			}
			
		};		
	}

	public void bindLinkToLeftHandle (FileSystemLink link) {
		bindLinkStartToHandle (link, fs_node_left);
	}
	
	public void bindLinkToRightHandle (FileSystemLink link) {
		bindLinkStartToHandle (link, fs_node_right);
	}	

	
	private void bindLinkStartToHandle (FileSystemLink link, VBox handle) {
		
		link.bindStart( Bindings.add(layoutXProperty(), fs_node_image.widthProperty().divide(2.0)),
						Bindings.add(layoutYProperty(), fs_node_title.heightProperty().add(fs_node_image.heightProperty().divide(2.0)))
						);
	}

	private void buildLinkDragHandlers() {
		
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

				mDragLink.setStart(mDragContext.sceneToLocal(p));
				
                ClipboardContent content = new ClipboardContent();
                DragDropContainer container = new DragDropContainer ();

                container.addData(SyncBindingProperty.SOURCE.toString(), 
                		((FileSystemNode) handle.getParent().getParent()).getId());
                
                content.put(DragDropContainer.AddBinding, container);
				
				handle.startDragAndDrop (TransferMode.ANY).setContent(content);	
				
				event.consume();
			}				
		};
		
		mLinkHandleDragDropped = new EventHandler <DragEvent> () {

			@Override
			public void handle(DragEvent event) {
				
				//get the drag data.  If it's null, abort.  
				//This isn't the drag event we're looking for.

				DragDropContainer container = 
						(DragDropContainer) event.getDragboard().getContent(DragDropContainer.AddBinding);
							
				if (container == null)
					return;
			
				VBox hndl = (VBox) event.getSource();
				
				ClipboardContent content = new ClipboardContent();
								
				container.addData(SyncBindingProperty.TARGET.toString(), 
						((FileSystemNode) hndl.getParent().getParent()).getId());
		
				content.put(DragDropContainer.AddBinding, container);
				
				event.getDragboard().setContent(content);
				
			}
			
		};

		//drop event for link creation
		mContextLinkDragDropped = new EventHandler <DragEvent> () {

			@Override
			public void handle(DragEvent event) {
		
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
			
				if (!mDragLink.isVisible())
					mDragLink.setVisible(true);
				
				event.acceptTransferModes(TransferMode.ANY);
				
				mDragLink.setEnd(event.getX(), event.getY());

				event.consume();
			}
		};		
	}
}
