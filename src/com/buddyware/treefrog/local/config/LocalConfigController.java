package com.buddyware.treefrog.local.config;

import java.awt.TextArea;

import com.buddyware.treefrog.BaseController;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;

public class LocalConfigController extends BaseController {

	@FXML
	private TextArea taLocalConfigErrorConsole;
	
	@FXML
	private TextArea taLocalConfigControllerConsole;
	
    /**
     * FXML initialization requirement
     */
    @FXML
    private void initialize() {
    }
}
