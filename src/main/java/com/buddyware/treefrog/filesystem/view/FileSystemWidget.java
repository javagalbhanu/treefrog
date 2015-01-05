package com.buddyware.treefrog.filesystem.view;

import java.io.IOException;

import com.buddyware.treefrog.filesystem.FileSystemType;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

public class FileSystemWidget extends AnchorPane {
	
	@FXML
	private AnchorPane root;
	
	@FXML
	private Label fs_container;
	
	@FXML
	private Region fs_image;
	
	private FileSystemType mType;
	
	public FileSystemWidget(FileSystemType fs_type) {
		
		mType = fs_type;
		
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource("/FileSystemWidget.fxml")
				);
		
		fxmlLoader.setRoot(this); 
		fxmlLoader.setController(this);
		
		try { 
			fxmlLoader.load();
        
		} catch (IOException exception) {
		    throw new RuntimeException(exception);
		}
	}
	
	public FileSystemType getFsType() { return mType; }
	
	@FXML
	private void initialize() {

		root.getStyleClass().add("filesystem-background");
		
		setId(mType.toString());
		
		switch (mType) {
		
		case AMAZON_S3:
			root.getStyleClass().add("filesystem-amazon-s3");
		break;
		
		case LOCAL_DISK:
			root.getStyleClass().add("filesystem-local-disk");
		break;
		
		}
		
		System.out.println("widg.init()");
	}
	
	public void relocateToPoint (Point2D p) {
		relocate (
				(int) (p.getX() - getBoundsInLocal().getWidth() / 2),
				(int) (p.getY() - getBoundsInLocal().getHeight() / 2)
			);
	}
	
	public void enableDragMode() {
		
		setId(getId() + "_drag");
		
    	setOpacity(0.5);
        setVisible(false);
        setMouseTransparent(true);
    	toFront();		
	}
}