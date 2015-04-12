package com.buddyware.treefrog.filesystem.view;

import java.io.IOException;

import com.buddyware.treefrog.filesystem.FileSystemType;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;;

public class FileSystemWidget extends AnchorPane {
	
	@FXML
	private AnchorPane root;
	
	@FXML
	private Label fs_container;
	
	@FXML
	private Pane fs_image;
	
	private final FileSystemType mType;
	private final FileSystemWidget mDragWidget;

	private FileSystemWidget (FileSystemType fs_type, boolean flag) {

		mDragWidget = null;
		mType = fs_type;
		setVisible(false);
		this.setMouseTransparent(true);
		
		loadWidget();
	}
	
	public FileSystemWidget(FileSystemType fs_type) {
		
		mType = fs_type;
		mDragWidget = new FileSystemWidget (fs_type, true);

		loadWidget();	
	}
	
	private void loadWidget() {
		
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
		
		if (mDragWidget==null)
			return;

	}
	
	public FileSystemWidget getDragWidget () { return mDragWidget; }

	public void relocateToPoint (Point2D p) {

		relocate (
				(int) (p.getX() - (getBoundsInLocal().getWidth() / 2)),
				(int) (p.getY() - (getBoundsInLocal().getHeight() / 2))
			);
	}
}