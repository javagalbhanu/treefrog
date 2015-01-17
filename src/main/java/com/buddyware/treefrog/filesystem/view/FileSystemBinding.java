package com.buddyware.treefrog.filesystem.view;

import java.io.IOException;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;

public class FileSystemBinding extends AnchorPane{

	private final Pane mDragContext;
	
	@FXML private CubicCurve left_link;
	@FXML private CubicCurve right_link;
	
	@FXML private Circle fs_left_link_handle;
	@FXML private Circle fs_right_link_handle;
	
	public FileSystemBinding(Pane drag_context) {
		mDragContext = drag_context;
/*
		fs_left_link_handle.setOnDragDetected(new EventHandler <MouseEvent>() {
		
			@Override
			public void handle(MouseEvent event) {
				
			}
		});
	*/	
		loadWidget();
	}
	
	public void initialize() {
		
		bindLinkEnds();
		
	};
	
	public void relocateToPoint (Point2D p) {
	
		if (left_link.startXProperty().isBound()) {
			right_link.startXProperty().set(p.getX());
			right_link.startYProperty().set(p.getY());
		}
		else {
			left_link.startXProperty().set(p.getX());
			left_link.startYProperty().set(p.getY());
		}
		
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
	
	public void bindLeftLink (DoubleProperty x_prop, DoubleProperty y_prop) {
		
		left_link.startXProperty().bind(x_prop);
		left_link.startYProperty().bind(y_prop);
		
		left_link.controlX1Property().bind(Bindings.add(x_prop, 100));
		left_link.controlY1Property().bind(y_prop);
	}
	
	public void bindRightLink (DoubleProperty x_prop, DoubleProperty y_prop) {
		right_link.startXProperty().bind(x_prop);
		right_link.startYProperty().bind(y_prop);

		right_link.controlX1Property().bind(Bindings.add(x_prop, -100));
		right_link.controlY1Property().bind(y_prop);
	}
	
	private void bindLinkEnds() {
		
		//bind the ends of the cubic curve which attach to the link node box
		//to the left and right handles.
		left_link.endXProperty().bind(fs_left_link_handle.centerXProperty());
		left_link.endYProperty().bind(fs_left_link_handle.centerYProperty());
		right_link.endXProperty().bind(fs_right_link_handle.centerXProperty());
		right_link.endYProperty().bind(fs_right_link_handle.centerYProperty());
		
		left_link.controlX2Property().bind(Bindings.add(fs_left_link_handle.centerXProperty(), -100));
		right_link.controlX2Property().bind(Bindings.add(fs_right_link_handle.centerXProperty(), 100));
		
		left_link.controlY2Property().bind(fs_left_link_handle.centerYProperty());
		right_link.controlY2Property().bind(fs_right_link_handle.centerYProperty());
		
	}
}
