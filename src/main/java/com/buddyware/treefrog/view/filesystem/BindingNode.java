package com.buddyware.treefrog.view.filesystem;

import java.net.URL;

import com.buddyware.treefrog.view.CustomFxml;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Ellipse;

public class BindingNode extends AnchorPane{

	private final BindingLink mLeftLink;
	private final BindingLink mRightLink;
	private final Pane mContext;
	
	@FXML private Ellipse node_ellipse;
	@FXML private AnchorPane node_background;
	
	private static final URL mBindingNodePath = 
			BindingNode.class.getResource("/BindingNode.fxml");
	
	private double mNodeWidth;
	private double mNodeHeight;
	
	public BindingNode(Pane context) {

		mLeftLink = new BindingLink();
		mRightLink = new BindingLink();
		mContext = context;
		
		CustomFxml.load (mBindingNodePath, this);
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
		
		bindLinkEnds();
		addNodePositionDragEvents();
		
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

	private void bindLinkEnds () {
		
		DoubleBinding xBinding = Bindings.add(node_background.layoutXProperty(), mNodeWidth / 2.0 );
		DoubleBinding yBinding = Bindings.add(node_background.layoutYProperty(), mNodeHeight / 2.0 );
	
		mLeftLink.endXProperty().bind (xBinding);
		mLeftLink.endYProperty().bind (yBinding);
	
		mRightLink.endXProperty().bind (xBinding);
		mRightLink.endYProperty().bind(yBinding);
	}
	
	public DoubleProperty leftStartXProperty() { return mLeftLink.startXProperty(); }
	public DoubleProperty leftStartYProperty() { return mLeftLink.startYProperty(); }
	public DoubleProperty rightStartXProperty() { return mRightLink.startXProperty(); }
	public DoubleProperty rightStartYProperty() { return mRightLink.startYProperty(); }
	
	
	public void bindToNodes (FileNode source, FileNode target) {
		
		node_background.setLayoutX(
						(mLeftLink.startXProperty().get() + 
						 mRightLink.startXProperty().get()) / 2.0
						);
		
		node_background.setLayoutY(
						(mLeftLink.startYProperty().get() + 
						 mRightLink.startYProperty().get()) / 2.0
						);
	}
}
