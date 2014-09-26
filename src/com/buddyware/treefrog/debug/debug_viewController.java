package com.buddyware.treefrog.debug;

import java.util.ArrayList;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.util.TaskMessage;
import com.buddyware.treefrog.util.TaskMessage.TaskMessageType;

public class debug_viewController extends BaseController {
	
	boolean debugConsoleIsVisible = false;
	
	@FXML
	Label watchServiceStatusLabel;
	
	@FXML
	Button watchServiceStartButton;
	
	@FXML
	Button watchServiceKillButton;
	
	@FXML
	TextArea debugConsoleOutputArea;
	
	@FXML
	TextArea debugConsoleErrorArea;
	
	@FXML
	Label lblTest;
	
    @FXML
    private void initialize() {

    	//mMain.addServiceStateListener("watch", createServiceStateListener ("watch"));
    
		//poll service blocking queues for ui updates
		AnimationTimer animTimer = new AnimationTimer() {
	
	        @Override
	        public void handle(long now) {
      	/*
                final ArrayList<TaskMessage> messages = mMain.getLocalFileModel().pollMessages();
                
                if (messages.isEmpty())
                	return; 

                for (TaskMessage message: messages) {

                	switch (message.getMessageType()) {
                	
                	case TASK_ACTIVITY:
                		
	                	Platform.runLater ( 
	                			() ->  debugConsoleOutputArea.appendText(message.getMessage() + "\n") 
	                			);
	                break;
	                
                	case TASK_ERROR:

	                	Platform.runLater ( 
	                			() ->  debugConsoleErrorArea.appendText(message.getMessage() + "\n")
	                			);
	                break;
	                
	                default:
	                	break;
                	}
                		
                }*/
	        }
		};
		
		animTimer.start();

    }
    
    private ChangeListener <Worker.State> createServiceStateListener (String serviceName) {
    	return new ChangeListener <Worker.State> () {
    		@Override
    		public void changed (ObservableValue <? extends State> message,
    				State arg1, State arg2) {

    			Platform.runLater( () -> updateServiceStateControls(message));
    		}    		
    	};
    };
    
    private void updateServiceStateControls(ObservableValue<? extends State> message) {
    	
		watchServiceStatusLabel.setText(message.getValue().toString());   

		watchServiceStartButton.setDisable(
				(message.getValue() == State.RUNNING) ||
				(message.getValue() == State.SCHEDULED)
				);

		watchServiceKillButton.setDisable(!watchServiceStartButton.isDisabled());
    }
    
    private ChangeListener <String> createListener (TextArea textarea) {

    	return new ChangeListener<String>() {
			@Override
			public void changed (ObservableValue <? extends String> message,
					String arg1, String arg2) {


				Platform.runLater(
						() -> textarea.appendText(message.getValue().toString()));
			}
    	};
	};
    
    @FXML
    private void startWatchService () { 
//System.out.println ("Starting watch esrvice");

    //	mMain.getLocalFileModel().startWatchService();
    	watchServiceStartButton.setDisable (true);
    };
    
    @FXML
    private void killWatchService () {
    	mMain.getLocalFileModel().killWatchService();
    	watchServiceKillButton.setDisable (true);
    };
}
