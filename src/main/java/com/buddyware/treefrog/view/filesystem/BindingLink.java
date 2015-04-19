package com.buddyware.treefrog.view.filesystem;

import java.net.URL;

import com.buddyware.treefrog.view.CustomFxml;

import javafx.beans.binding.Bindings;

import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.CubicCurve;

public class BindingLink extends Pane{

	@FXML CubicCurve fs_link;
	
	private final DoubleProperty mControlOffsetX = new SimpleDoubleProperty();
	private final DoubleProperty mControlOffsetY = new SimpleDoubleProperty();
	private final DoubleProperty mControlDirectionX1 = new SimpleDoubleProperty();
	private final DoubleProperty mControlDirectionY1 = new SimpleDoubleProperty();
	private final DoubleProperty mControlDirectionX2 = new SimpleDoubleProperty();
	private final DoubleProperty mControlDirectionY2 = new SimpleDoubleProperty();
	
	private static final URL mBindingLinkPath =
			BindingLink.class.getResource("/BindingLink.fxml");
	
	public BindingLink() {
		CustomFxml.load(mBindingLinkPath, this);
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

	public DoubleProperty startXProperty() { return fs_link.startXProperty(); }
	public DoubleProperty startYProperty() { return fs_link.startYProperty(); }
	public DoubleProperty endXProperty() { return fs_link.endXProperty(); }
	public DoubleProperty endYProperty() { return fs_link.endYProperty(); }
	
	public void setStart (Point2D p) {
		fs_link.startXProperty().set(p.getX());
		fs_link.startYProperty().set(p.getY());
	}
	
	public void setEnd (Point2D p) {
		fs_link.endXProperty().set(p.getX());
		fs_link.endYProperty().set(p.getY());
	}
}
