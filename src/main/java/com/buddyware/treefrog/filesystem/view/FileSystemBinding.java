package com.buddyware.treefrog.filesystem.view;

import java.io.IOException;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Ellipse;

public class FileSystemBinding extends AnchorPane{

	private final FileSystemLink mLeftLink;
	private final FileSystemLink mRightLink;
	private final Pane mContext;
	
	@FXML private Ellipse node_ellipse;
	@FXML private AnchorPane node_background;
	
	private double mNodeWidth;
	private double mNodeHeight;
	
	public FileSystemBinding(Pane context) {

		mLeftLink = new FileSystemLink();
		mRightLink = new FileSystemLink();
		mContext = context;
		
		loadWidget();
	}
	
	@FXML
	private void initialize() {

	   Group root = new Group();

	   root.getChildren().add(node_background);
	   root.applyCss();
	   root.layout();
		
		mContext.getChildren().add(0, mLeftLink);
		mContext.getChildren().add(0, mRightLink);
		
		mNodeWidth = node_background.getLayoutBounds().getWidth();
		mNodeHeight = node_background.getLayoutBounds().getHeight();
		
		DoubleBinding xBinding = Bindings.add(node_background.layoutXProperty(), mNodeWidth / 2.0 );
		DoubleBinding yBinding = Bindings.add(node_background.layoutYProperty(), mNodeHeight / 2.0 );
		
		mLeftLink.bindEnd (xBinding, yBinding);
		mRightLink.bindEnd (xBinding, yBinding);
		
		addNodePositionDragEvents();
		
	}
	
	private void loadWidget() {
		
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource("/FileSystemBinding.fxml")
				);
		
		fxmlLoader.setRoot(this); 
		fxmlLoader.setController(this);

		try { 
			fxmlLoader.load();
        
		} catch (IOException exception) {
		    throw new RuntimeException(exception);
		}		
	}
	
	public void addNodePositionDragEvents() {
		
		node_background.setOnDragDetected(new EventHandler <MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				
				node_background.getParent().setOnDragDropped(null);
				node_background.getParent().setOnDragOver(null);
				
				node_background.getParent().setOnDragOver( new EventHandler <DragEvent> () {

					@Override
					public void handle(DragEvent event) {
						node_background.setLayoutX(event.getX() - (node_background.getWidth() / 2.0));
						node_background.setLayoutY(event.getY() - (node_background.getHeight() / 2.0));
					}
					
				});
				
                ClipboardContent content = new ClipboardContent();

                content.putString("bindingnode");

				node_background.startDragAndDrop (TransferMode.ANY).setContent(content);					
			}
			
		});
	}

	public void bindLinksToNodes (FileSystemNode source, FileSystemNode target) {
		
		source.bindLinkToLeftHandle(mLeftLink);
		mLeftLink.setControlOffsets(new Point2D (5.0, 0.0));

		target.bindLinkToRightHandle(mRightLink);
		mRightLink.setControlOffsets(new Point2D (5.0, 0.0));
		
		node_background.setLayoutX(((mLeftLink.getStartX() + mRightLink.getStartX()) / 2.0));
		node_background.setLayoutY(((mLeftLink.getStartY() + mRightLink.getStartY()) / 2.0));
	}

}
