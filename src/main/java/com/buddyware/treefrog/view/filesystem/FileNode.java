package com.buddyware.treefrog.view.filesystem;

import java.net.URL;

import com.buddyware.treefrog.utils;
import com.buddyware.treefrog.model.filesystem.FileSystem;
import com.buddyware.treefrog.model.filesystem.FileSystemProperty;
import com.buddyware.treefrog.model.filesystem.FileSystemType;
import com.buddyware.treefrog.model.syncbinding.SyncBindingProperty;
import com.buddyware.treefrog.view.CustomFxml;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FileNode extends AnchorPane {

	@FXML private AnchorPane fs_node_title;
	@FXML private AnchorPane fs_node_image;
	@FXML private Label fs_node_title_bar;
	
	@FXML private VBox fs_node_left;
	@FXML private VBox fs_node_right;
	
	private static final URL mFileNodePath = 
			FileNode.class.getResource("/FileNode.fxml"); 
	
	private InvalidationListener mModelUpdateListener = null;
	
	private FileSystemType mFsType;
	private Point2D mDragPoint;

	private BindingLink mDragLink;
	
	private FileSystem mModel;
	
	private EventHandler <DragEvent> mContextDragOver;
	private EventHandler <DragEvent> mContextDragDropped;
	private EventHandler <DragEvent> mContextLinkDragOver;
	private EventHandler <DragEvent> mContextLinkDragDropped;

	
	private final FileNode mSelf;
	
	public FileNode(FileSystemType fs_type, FileSystem fs) {
		
		mFsType = fs_type;
		mModel = fs;
		
		CustomFxml.load(mFileNodePath, this);
		CustomFxml.getInstance().setStyle(fs_node_image, mFsType);
		
		setId(mFsType.toString());
		setTitle(mFsType.toString());
		
		buildNodeDragHandlers();
		buildLinkDragHandlers();		
		
		mSelf = this;
		
	}

	public void addModelUpdateListener (InvalidationListener listener) {
		mModelUpdateListener = listener;
	}
	
	public void setTitle (String text) { fs_node_title_bar.setText(text); }
	
	public String getTitle() { return fs_node_title_bar.getText(); }

	private void relocateToPoint (Point2D p) {
		
		Point2D p2 = getParent().sceneToLocal(p);

		relocate (
				(int) (p2.getX() - mDragPoint.getX()),
				(int) (p2.getY() - mDragPoint.getY())
			);
	}
	
	public void bindToLink (DoubleProperty xProperty, DoubleProperty yProperty) {
		
		xProperty.bind(
				Bindings.add(
						layoutXProperty(), fs_node_image.widthProperty().divide(2.0)
				)
		);

		yProperty.bind(
				Bindings.add(
						layoutYProperty(), fs_node_title.heightProperty().add(
								fs_node_image.heightProperty().divide(2.0)
						)
				)
		);
	}	
	
	private void createDragLink(Point2D startPoint) {

		if (mDragLink == null) {
			mDragLink = new BindingLink();
			mDragLink.setId("drag link");
		}
		
		mDragLink.setVisible(false);
		
		Pane parent = (Pane) getParent();
		parent.getChildren().add(0,mDragLink);
		
		mDragLink.setStart(getParent().sceneToLocal(startPoint));		
	}
	
	private void destroyDragLink() {
		
		Pane parent = (Pane) getParent();

		for (int i = 0; i < parent.getChildren().size(); i++){
			if (parent.getChildren().get(i).getId() == "drag link")
				parent.getChildren().remove(i);
		}
	}
	
	public void buildNodeDragHandlers() {
		
		//context drag over for node dragging
		mContextDragOver = (DragEvent e) -> {

			e.acceptTransferModes(TransferMode.ANY);				
			relocateToPoint(new Point2D( e.getSceneX(), e.getSceneY()));
			e.consume();
		};
		
		//context drop for node dragging
		mContextDragDropped = (DragEvent e) -> {
	
			getParent().setOnDragOver(null);
			getParent().setOnDragDropped(null);
			
			DragContent.add (e, DragContent.MoveNode,
					FileSystemProperty.LAYOUT_X.toString(),
					Double.toString (mSelf.getLayoutX()));
			
			DragContent.add (e, DragContent.MoveNode,
					FileSystemProperty.LAYOUT_Y.toString(),
					Double.toString (mSelf.getLayoutY()));
			
			e.setDropCompleted(true);
			e.consume();
		};

		//beings node dragging
		fs_node_title.setOnDragDetected(
			(MouseEvent e) -> {

				getParent().setOnDragOver(null);
				getParent().setOnDragDropped(null);
	
				getParent().setOnDragOver (mContextDragOver);
				getParent().setOnDragDropped (mContextDragDropped);
				
				mDragPoint = new Point2D(e.getX(), e.getY());
	
	            //begin drag ops
	            relocateToPoint(new Point2D(e.getSceneX(), e.getSceneY()));
				
	            startDragAndDrop (TransferMode.ANY).setContent(
	            		
	            		DragContent.create (DragContent.MoveNode,
	            				FileSystemProperty.ID.toString(),
	            				mSelf.getId()
	            				)
	            		);
	            
	            e.consume();					
			}
		);
		
		//spawns config box when title is clicked
		fs_node_title.setOnMouseClicked( (MouseEvent e) -> {

			final Stage stage = new Stage();
			
			FileNodeConfig controller = utils.<BorderPane, FileNodeConfig>loadFxml
								("/FileSystemConfig.fxml", stage , null);
			
			controller.setType(mFsType);
			controller.setModel (mModel);
			controller.addModelUpdateListener(mModelUpdateListener);
		});		
	}
	
	private void buildLinkDragHandlers() {
		
		//context drop of dragging links
		mContextLinkDragDropped = (DragEvent e) -> {

			getParent().setOnDragOver(null);
			getParent().setOnDragDropped(null);
			
			mSelf.destroyDragLink();

			e.consume();
		};

		//context dragover for dragging links
		mContextLinkDragOver = (DragEvent e) -> {	
			
			e.acceptTransferModes(TransferMode.ANY);

			if (!mDragLink.isVisible())
				mDragLink.setVisible(true);
			
			mDragLink.endXProperty().set(e.getX());
			mDragLink.endYProperty().set(e.getY());
			
			e.consume();
			
		};	
		
		//link handle drag detection
		EventHandler <MouseEvent> dragDetected = (MouseEvent e) -> {

			getParent().setOnDragOver(null);
			getParent().setOnDragDropped(null);
			
			getParent().setOnDragOver(mContextLinkDragOver);
			getParent().setOnDragDropped(mContextLinkDragDropped);

			VBox handle = (VBox) (e.getSource());
			
			Point2D p = handle.getParent().localToScene(
							handle.getLayoutX() + (getWidth() / 2.0),
							handle.getLayoutY() + (getHeight() / 2.0)
						);

			createDragLink(p);
			
			handle.startDragAndDrop (TransferMode.ANY).setContent(
					DragContent.create (DragContent.AddBinding,
							SyncBindingProperty.SOURCE.toString(), mSelf.getId()));	
			
			e.consume();
		};

		//link handle drag drop
		EventHandler <DragEvent> dragDropped = (DragEvent e) -> {

			DragContent.add (e, DragContent.AddBinding,
					SyncBindingProperty.TARGET.toString(), mSelf.getId());
			
			mSelf.destroyDragLink();
			
			e.consume();
		};
				
		//assign handlers that are permanent
		fs_node_left.setOnDragDetected(dragDetected);
		fs_node_right.setOnDragDetected(dragDetected);
		
		fs_node_left.setOnDragDropped(dragDropped);
		fs_node_right.setOnDragDropped(dragDropped);		
	}
}
