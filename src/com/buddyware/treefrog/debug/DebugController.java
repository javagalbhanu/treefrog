package com.buddyware.treefrog.debug;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import com.buddyware.treefrog.BaseController;

public class DebugController extends BaseController {
	
	boolean debugConsoleIsVisible = false;
	
	@FXML
	Label watchServiceStatusLabel;
	
	@FXML
	Button watchServiceStartButton;
	
	@FXML
	Button watchServiceKillButton;
	
	@FXML
	Button launchDebugConsoleButton;
	
	@FXML
	TextArea debugConsoleOutputArea;
	
	@FXML
	TextArea debugConsoleErrorArea;
	
    @FXML
    private void initialize() {

    	
		debugConsoleOutputArea.setText("text area text");
    	//mMain.addServiceStateListener("watch", createServiceStateListener ("watch"));
    }
    
    private ChangeListener <Worker.State> createServiceStateListener (String serviceName) {
    	return new ChangeListener <Worker.State> () {
    		@Override
    		public void changed (ObservableValue <? extends State> message,
    				State arg1, State arg2) {

				watchServiceStatusLabel.setText(message.getValue().toString());   

				watchServiceStartButton.setDisable(
						(message.getValue() == State.RUNNING) ||
						(message.getValue() == State.SCHEDULED)
						);
				
				watchServiceKillButton.setDisable(watchServiceStartButton.isDisabled());

    		}    		
    	};
    };
    
    private ChangeListener <String> createTextAreaChangeListener (TextArea textarea) {
    
    	return new ChangeListener<String>() {
			@Override
			public void changed (ObservableValue <? extends String> message,
					String arg1, String arg2) {
				System.out.println("textarea value " + message.getValue());

					//textarea.setText(message.getValue());
			}
    	};
	};
    
    @FXML
    private void startWatchService () { 
    //	mMain.startService ("watch");
    	watchServiceStartButton.setDisable (true);
    };
    
    @FXML
    private void killWatchService () {
    //	mMain.killService("watch");
    	watchServiceKillButton.setDisable (true);
    };
}
