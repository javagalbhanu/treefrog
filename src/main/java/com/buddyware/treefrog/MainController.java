package com.buddyware.treefrog;

import javafx.fxml.FXML;
import javafx.stage.Stage;

public class MainController extends BaseController {

	public MainController() {

		/*		
*/
	}

	@FXML
	public void initialize() {

	}

	public Stage getParentStage() {

		return parentStage;

	}
	/*
	 * public void addServiceStateListener (String serviceName, ChangeListener
	 * listener) {
	 * 
	 * switch (serviceName) {
	 * 
	 * case "watch": localModel.watchServiceState().addListener(listener);
	 * break;
	 * 
	 * default: break;
	 * 
	 * } }
	 */
}
