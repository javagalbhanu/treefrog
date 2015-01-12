package com.buddyware.treefrog.filesystem.view;

import java.io.IOException;

import com.buddyware.treefrog.filesystem.FileSystemType;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

public class FileSystemWidget extends AnchorPane implements IFileSystemObject{
	
	@FXML
	private AnchorPane root;
	
	@FXML
	private Label fs_container;
	
	@FXML
	private Region fs_image;
	
	private FileSystemType mType;
	private FileSystemWidget mDragWidget;
	
	public FileSystemWidget() {
			
	}
	
	public String getFileSystemObjectType() { return "FileSystemWidget"; }
	
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
	
	public IFileSystemObject getDragObject() {
		
		if (mDragWidget == null)
			createDragWidget();
		
		return mDragWidget; 
	}
	
	public FileSystemType getFileSystemType() { return mType; }
	
	@FXML
	private void initialize() {

		root.getStyleClass().add("fs-widget-background");
		
		setId(mType.toString());
		
		switch (mType) {
		
		case AMAZON_S3:
			root.getStyleClass().add("fs-widget-image");
			root.getStyleClass().add("fs-amazon-s3");
		break;
		
		case LOCAL_DISK:
			root.getStyleClass().add("fs-widget-image");			
			root.getStyleClass().add("fs-local-disk");
		break;
		
		}
	}
	
	public void initDrag(Point2D p) {
		
		if (mDragWidget == null)
			createDragWidget();
		
		mDragWidget.setVisible(true);
	}
	
	public void relocateToPoint (Point2D p) {
		
		Point2D p2 = this.getParent().sceneToLocal(p);
		
		relocate (
				(int) (p2.getX() - getBoundsInLocal().getWidth() / 2),
				(int) (p2.getY() - getBoundsInLocal().getHeight() / 2)
			);
	}
	
	private void createDragWidget() {
		
		mDragWidget = new FileSystemWidget (mType);
		
    	mDragWidget.setOpacity(0.5);
        mDragWidget.setVisible(false);
        mDragWidget.setMouseTransparent(true);
    	mDragWidget.toFront();
    	
	}
}