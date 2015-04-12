package com.buddyware.treefrog.filesystem.view;

import java.io.IOException;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.CubicCurve;

public class FileSystemLink extends Pane{

	@FXML CubicCurve fs_link;
	
	private final DoubleProperty mControlOffsetX = new SimpleDoubleProperty();
	private final DoubleProperty mControlOffsetY = new SimpleDoubleProperty();
	private final DoubleProperty mControlDirectionX1 = new SimpleDoubleProperty();
	private final DoubleProperty mControlDirectionY1 = new SimpleDoubleProperty();
	private final DoubleProperty mControlDirectionX2 = new SimpleDoubleProperty();
	private final DoubleProperty mControlDirectionY2 = new SimpleDoubleProperty();
	
	public FileSystemLink() {

		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource("/FileSystemLink.fxml")
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
		
	    mControlOffsetX.set(100.0);
		mControlOffsetY.set(50.0);		

		mControlDirectionX1.bind(new When (
				fs_link.startXProperty().greaterThan(fs_link.endXProperty()))
				.then(-1.0).otherwise(1.0));
		
		mControlDirectionX2.bind(new When (
				fs_link.startXProperty().greaterThan(fs_link.endXProperty()))
				.then(1.0).otherwise(-1.0));

		
		fs_link.controlX1Property().bind(
				Bindings.add(
						fs_link.startXProperty(), mControlOffsetX.multiply(mControlDirectionX1)
						)
				);
		
		fs_link.controlX2Property().bind(
				Bindings.add(
						fs_link.endXProperty(), mControlOffsetX.multiply(mControlDirectionX2)
						)
				);
		
		fs_link.controlY1Property().bind(
				Bindings.add(
						fs_link.startYProperty(), mControlOffsetY.multiply(mControlDirectionY1)
						)
				);
		
		fs_link.controlY2Property().bind(
				Bindings.add(
						fs_link.endYProperty(), mControlOffsetY.multiply(mControlDirectionY2)
						)
				);
	}
	
	public void setControlOffsets (Point2D controlOffsets) {
		mControlOffsetX.set(controlOffsets.getX());
		mControlOffsetY.set(controlOffsets.getY());
	}

	public double getStartX() { return fs_link.getStartX(); }
	public double getStartY() { return fs_link.getStartY(); }
	
	public void bindStart (NumberBinding x, NumberBinding y) {
		
		fs_link.startXProperty().bind(x);
		fs_link.startYProperty().bind(y);
	
	}
	
	public void bindEnd (DoubleProperty x, DoubleProperty y) {
		fs_link.endXProperty().bind(x);
		fs_link.endYProperty().bind(y);
		
	}
	
	public void bindEnd (DoubleBinding x, DoubleBinding y) {
		
		fs_link.endXProperty().bind(x);
		fs_link.endYProperty().bind(y);			
	}
	
	public void setStart (Point2D p) {
		setStart (p.getX(), p.getY());
	}
	
	public void setStart (double x, double y) {
		fs_link.startXProperty().set(x);
		fs_link.startYProperty().set(y);
	}
	
	public void setEnd (Point2D p) {
		setEnd (p.getX(), p.getY());
	}
	
	public void setEnd (double x, double y) {
		fs_link.endXProperty().set(x);
		fs_link.endYProperty().set(y);
	}
}
