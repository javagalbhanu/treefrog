package com.buddyware.treefrog.remote.config;

import java.util.ArrayList;
import java.util.List;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.remote.model.RemoteModel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class RemoteConfigController extends BaseController {

	
	private RemoteModel AWSConnection = null;
	
	
    /**
     * FXML initialization requirement
     */
    @FXML
    private void initialize() {
    }
    @FXML private ListView BucketList;
    
    @FXML
	private void on_click() {
    	AWSConnection = new RemoteModel();
    	ArrayList<String> ListOfBuckets = AWSConnection.ListBuckets();
    	ObservableList <String> ObservableListOfBuckets = FXCollections.observableList(ListOfBuckets);
    	    	
    	BucketList.setItems(ObservableListOfBuckets);
    	
      			
    			
	}
     
    
    @FXML
    private void send_test_file(){
    	
    }
    
}


