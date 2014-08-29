package com.buddyware.treefrog.local.config;

import com.buddyware.treefrog.BaseController;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LocalConfigController extends BaseController {

	@FXML
	private Label lblLocalConfigView;
	
    /**
     * FXML initialization requirement
     */
    @FXML
    private void initialize() {
    	lblLocalConfigView.textProperty().bind(mMain.message());

    	lblLocalConfigView.textProperty().addListener(new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> arg0,
					String arg1, String arg2) {
	             System.out.println("LocalFileView (lblConfigModel) message: " + (String)arg0.getValue());
			}
	      });    	
    }
    
    @FXML
    private void startWatchService() {
    	mMain.startWatchService();
    }
}
