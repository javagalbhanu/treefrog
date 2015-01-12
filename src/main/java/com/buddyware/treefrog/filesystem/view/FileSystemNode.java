package com.buddyware.treefrog.filesystem.view;

import java.io.IOException;

import com.buddyware.treefrog.filesystem.FileSystemType;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class FileSystemNode extends AnchorPane implements IFileSystemObject{

	@FXML private AnchorPane fs_node_image;
	@FXML private VBox fs_node_left;
	@FXML private VBox fs_node_right;
	
	private FileSystemType mFsType;
	private Point2D mDragPoint;
	
	public FileSystemNode(FileSystemType fs_type) {
		
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

		mFsType = fs_type;
		setId(fs_type.toString() + Double.toString(Math.random()));
		setFileSystemNodeImage();
	
	}

	public void initDrag(Point2D p) {
		mDragPoint = this.sceneToLocal(p);
	}
	
	public IFileSystemObject getDragObject() { return this; }
		
	public String getFileSystemObjectType() { return "FileSystemNode"; }
	
	public FileSystemType getFileSystemType() { return mFsType; }
	
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
		
		Point2D p2 = this.getParent().sceneToLocal(p);

		relocate (
				(int) (p2.getX() - mDragPoint.getX()),
				(int) (p2.getY() - mDragPoint.getY())
			);
	}	
}
