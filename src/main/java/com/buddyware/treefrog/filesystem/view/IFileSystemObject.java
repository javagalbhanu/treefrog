package com.buddyware.treefrog.filesystem.view;

import com.buddyware.treefrog.filesystem.FileSystemType;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

public interface IFileSystemObject {

		public String getId();
		public void relocateToPoint(Point2D point);
		public String getFileSystemObjectType();
		public Dragboard startDragAndDrop(TransferMode... transferModes);
		public void setOnDragDetected(EventHandler <? super MouseEvent> value);
		public Parent getParent();
		public IFileSystemObject getDragObject();
		public void setVisible (boolean visible);
		public FileSystemType getFileSystemType();
		public void initDrag(Point2D p);
}
