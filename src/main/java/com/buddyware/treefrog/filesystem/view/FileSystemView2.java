package com.buddyware.treefrog.filesystem.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.event.EventHandler;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.filesystem.FileSystemType;

public class FileSystemView2 extends AnchorPane {

	/**
	 * FXML initialization requirement
	 */
/*
	@FXML private Circle curveEnd;
	@FXML private Circle curveStart;
	@FXML private Circle curveC1;
	@FXML private Circle curveC2;

	@FXML private Label cloudLabel;
	@FXML private Line mLt_start_c1;
	@FXML private Line mLt_c2_end;
	@FXML private CubicCurve mCurve;
	*/
	@FXML private VBox fs_list;
	@FXML private SplitPane fs_split_pane;
	@FXML private AnchorPane fs_root;
	
	@FXML private Pane fs_right_pane;
	
	private FileSystemWidget mDragWidget;
	private Parent mSceneRoot = null;
	
	public FileSystemView2() {
		
		FXMLLoader fxmlLoader = new FXMLLoader(
				getClass().getResource("/FileSystemView2.fxml")
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

		//add source filesystem node to right pane
		addFileSystemWidget (FileSystemType.LOCAL_DISK);
		addFileSystemWidget (FileSystemType.AMAZON_S3);

		fs_split_pane.setOnDragOver((e) -> {
			if (mSceneRoot == null)
				mSceneRoot = fs_list.getScene().getRoot();
			
			mDragWidget.relocateToPoint(mSceneRoot.sceneToLocal(e.getSceneX(), e.getSceneY()));
			e.consume();
		});

		fs_root.setOnDragDone((e) -> {
			mDragWidget.setVisible(false);
			e.consume();
		});
		
		fs_right_pane.setOnDragOver ((e) -> {
			e.acceptTransferModes(TransferMode.ANY);
		});
		
		fs_right_pane.setOnDragDropped((e) -> {
		
			Dragboard db = e.getDragboard();
			JOptionPane.showMessageDialog(null, db.getString());
			mDragWidget.setVisible(false);
			e.setDropCompleted(true);
		});
	}

	private void addFileSystemWidget (FileSystemType fs_type) {

		FileSystemWidget widg = new FileSystemWidget (fs_type);
		FileSystemWidget drag_widg = new FileSystemWidget (fs_type);

		drag_widg.enableDragMode();
		
		fs_list.getChildren().add(widg);
        fs_root.getChildren().add(drag_widg);
		
		//add drag handling
		addWidgetDragHandler (widg);
	}
	
	private void addWidgetDragHandler (FileSystemWidget widg) {
		
		widg.setOnDragDetected( 
		new EventHandler <MouseEvent>() {
	    	 
			private final FileSystemType mType = widg.getFsType();
			
            @Override
            public void handle(MouseEvent t) {
                
            	mDragWidget = null;
            	String idMatch = mType.toString() + "_drag";
            	
            	//find the corresponding drag widget for the selected filesystemwidget
                for (Node child: fs_root.getChildren()) {
              	
                	if (!child.getId().equals(idMatch))
                		continue;
                	
                	mDragWidget = (FileSystemWidget) child;
                	break;
                }

                if (mDragWidget == null)
                	return;

                //begin drag ops
                ClipboardContent content = new ClipboardContent();
                content.putString(widg.getFsType().toString());

                mDragWidget.relocateToPoint(new Point2D(t.getSceneX(), t.getSceneY()));
                mDragWidget.startDragAndDrop(TransferMode.ANY).setContent(content);
                mDragWidget.setVisible(true);
                
                t.consume();
            }
        });
	}
	/*
	private void initCurves() {
		
		// bind control lines to circle centers
		mLt_start_c1.startXProperty().bind(mCurve.startXProperty());
		mLt_start_c1.startYProperty().bind(mCurve.startYProperty());

		mLt_start_c1.endXProperty().bind(mCurve.controlX1Property());
		mLt_start_c1.endYProperty().bind(mCurve.controlY1Property());

		mLt_c2_end.startXProperty().bind(mCurve.controlX2Property());
		mLt_c2_end.startYProperty().bind(mCurve.controlY2Property());

		mLt_c2_end.endXProperty().bind(mCurve.endXProperty());
		mLt_c2_end.endYProperty().bind(mCurve.endYProperty());

		// bind curve to circle centers
		mCurve.startXProperty().bind(curveStart.centerXProperty());
		mCurve.startYProperty().bind(curveStart.centerYProperty());

		mCurve.controlX1Property().bind(curveC1.centerXProperty());
		mCurve.controlY1Property().bind(curveC1.centerYProperty());

		mCurve.controlX2Property().bind(curveC2.centerXProperty());
		mCurve.controlY2Property().bind(curveC2.centerYProperty());

		mCurve.endXProperty().bind(curveEnd.centerXProperty());
		mCurve.endYProperty().bind(curveEnd.centerYProperty());

		curveStart.setCenterX(10.0f);
		curveStart.setCenterY(10.0f);

		curveC1.setCenterX(20.0f);
		curveC1.centerXProperty().bind(Bindings.add(150.0f, curveStart.centerXProperty()));
		curveC1.centerYProperty().bind(curveStart.centerYProperty());

		curveC2.setCenterX(50.0f);
		curveC2.centerXProperty().bind(Bindings.add(-150.0f, curveEnd.centerXProperty()));
		curveC2.centerYProperty().bind(curveEnd.centerYProperty());

		curveEnd.setCenterX(40.0f);
		curveEnd.setCenterY(40.0f);		
	}

	@FXML
	private void updateCurveStart(MouseEvent event) {

		curveStart.setCenterX(event.getX());
		curveStart.setCenterY(event.getY());
	}

	@FXML
	private void updateCurveC1(MouseEvent event) {

		curveC1.setCenterX(event.getX());
		curveC1.setCenterY(event.getY());
	}

	@FXML
	private void updateCurveC2(MouseEvent event) {

		curveC2.setCenterX(event.getX());
		curveC2.setCenterY(event.getY());
	}

	@FXML
	private void updateCurveEnd(MouseEvent event) {

		curveEnd.setCenterX(event.getX());
		curveEnd.setCenterY(event.getY());
	}*/
}
