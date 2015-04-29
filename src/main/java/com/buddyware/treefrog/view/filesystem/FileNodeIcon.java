package com.buddyware.treefrog.view.filesystem;

import java.net.URL;

import com.buddyware.treefrog.model.filesystem.FileSystemType;
import com.buddyware.treefrog.view.CustomFxml;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;;

public class FileNodeIcon extends AnchorPane {
	
	@FXML private AnchorPane root;
	@FXML private Label fs_container;
	@FXML private Pane fs_image;
	
	private final static URL mFileNodeIconPath = 
			FileNode.class.getResource("/FileNodeIcon.fxml"); 
	
	private final FileSystemType mType;
	private final FileNodeIcon mDragWidget;

	private FileNodeIcon (FileSystemType fs_type, boolean flag) {

		mDragWidget = null;
		mType = fs_type;
		setVisible(false);
		
		this.setMouseTransparent(true);
		
		CustomFxml.load(mFileNodeIconPath, this);
	}
	
	public FileNodeIcon(FileSystemType fs_type) {
		
		mDragWidget = new FileNodeIcon (fs_type, true);
		mType = fs_type;
		
		CustomFxml.load (mFileNodeIconPath, this);	
	}
	
	public FileSystemType getFileSystemType() { return mType; }
	
	@FXML
	private void initialize() {

		setId(mType.toString());
		
		root.getStyleClass().add("fs-widget-background");
		root.getStyleClass().add("fs-widget-image");
		
		switch (mType) {
		
		case AMAZON_S3:
			root.getStyleClass().add("fs-amazon-s3");
		break;
		
		case LOCAL_DISK:
			root.getStyleClass().add("fs-local-disk");
		break;

		default:
		break;
		}
	}
	
	public FileNodeIcon getDragWidget () { return mDragWidget; }

	public void relocateToPoint (Point2D p) {

		relocate (
				(int) (p.getX() - (getBoundsInLocal().getWidth() / 2)),
				(int) (p.getY() - (getBoundsInLocal().getHeight() / 2))
			);
	}
}