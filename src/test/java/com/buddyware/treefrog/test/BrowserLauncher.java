package com.buddyware.treefrog.test;

import java.awt.AWTException;

import org.jemmy.fx.AppExecutor;
import org.jemmy.fx.Browser;
import org.junit.BeforeClass;
import org.junit.Test;

import com.buddyware.treefrog.Main;

public class BrowserLauncher {

	@BeforeClass
	public static void setUpClass() {
		AppExecutor.executeNoBlock(Main.class);
	}
/*
    public static void main(String[] args) throws AWTException {
    	
    	AppExecutor.executeNoBlock(Main.class);
    	Browser.runBrowser();
    }
    */
    @Test
    public void hello() {

    }
}
