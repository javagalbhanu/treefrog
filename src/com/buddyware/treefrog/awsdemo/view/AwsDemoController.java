package com.buddyware.treefrog.awsdemo.view;

import java.io.File;
import java.io.IOException;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import com.amazonaws.AmazonClientException;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.transfer.Upload;
import com.buddyware.treefrog.Main;
import com.buddyware.treefrog.awsdemo.model.S3TransferSampleModel;
import com.buddyware.treefrog.util.FileChooserController;

/**
 * This sample demonstrates how to make basic requests to Amazon S3 using
 * the AWS SDK for Java.
 * <p>
 * <b>Prerequisites:</b> You must have a valid Amazon Web Services developer
 * account, and be signed up to use Amazon S3. For more information on
 * Amazon S3, see http://aws.amazon.com/s3.
 * <p>
 * <b>Important:</b> Be sure to fill in your AWS access credentials in the
 *                   AwsCredentials.properties file before you try to run this
 *                   sample.
 * http://aws.amazon.com/security-credentials
 */

public class AwsDemoController {

	@FXML
	private ProgressBar progressBar;
	
	private S3TransferSampleModel s3Transfer;
	
	private Main mainApp;
	
	private Stage parentStage;
	
	/**
	 * AWS demo code
	 */

	public AwsDemoController () {
		
		this.s3Transfer = new S3TransferSampleModel();
	}
	
    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {

    }
    
    public void setParentStage ( Stage stage) {
    	parentStage = stage;
    }
    
	@FXML
	private void uploadFile() {
		
		//Task task = new Task<Void>() {
		 //   @Override public Void call() throws Exception {
		    		System.out.println("uploadFile() called!");
        // Load the fxml file and create a new stage for the popup dialog.
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("view/Dialog.fxml"));
        
        FileChooserController ctlFileChooser = new FileChooserController("Choose a file to upload", parentStage);

        loader.setController (ctlFileChooser);
        ctlFileChooser.setMultiSelect(false);
        ctlFileChooser.show();
        
        for (File f: ctlFileChooser.selectedFiles()) {

        	ProgressListener listener = createProgressListener (f.length());
        	
        	s3Transfer.transferFile(f, listener);
        }
	};

    private ProgressListener createProgressListener(long fileLength) {
    	System.out.println("creating progress listener...");
    	//need to rewrite to post event back to JavaFX for error message
    	
        return new ProgressListener() {

        	private final float fLen = fileLength;
        	private float curValue = 0;
        	
            public void progressChanged (ProgressEvent progressEvent) {
           	
                if (fLen == -1) return;

                curValue += progressEvent.getBytesTransferred();
                System.out.println(curValue + " / " + fLen);

				progressBar.setProgress (curValue / fLen);
                switch (progressEvent.getEventType()) {
                
                case TRANSFER_COMPLETED_EVENT:
                    progressBar.setProgress(100);
                    break;
                    
                case TRANSFER_FAILED_EVENT:
                   /* try {
                       // AmazonClientException e = upload.waitForException();
                        
                        JOptionPane.showMessageDialog(frame,
                                "Unable to upload file to Amazon S3: " + e.getMessage(),
                                "Error Uploading File", JOptionPane.ERROR_MESSAGE);
                                
                    } catch (InterruptedException e) {}
                    */
                    break;
                    
                default:
                	break;
                }
            }
        };
    }
}
