package com.buddyware.treefrog.stats;

import java.lang.reflect.InvocationTargetException;

import com.buddyware.treefrog.BaseController;

import javafx.fxml.FXML;

public class StatsController extends BaseController {

    /**
     * FXML initialization requirement
     */
    @FXML
    private void initialize() {
    	System.out.println ("Stats controller initialized!");
    }
    
    public void testStatsComm() {
    	System.out.println ("Stats controller comm!");
    	//BaseController.mMain.testStatsControllerComm();
    }
    /*
    public void testStatsControllerComm() throws InvocationTargetException {
    	System.out.println ("Stats controller comm from Main!");
    }*/
}
