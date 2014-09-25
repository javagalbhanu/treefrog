package com.buddyware.treefrog.awsdemo.model;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

public class S3TransferSampleModel {

	private static AWSCredentials credentials = null;
    private static TransferManager tx;
    private static String bucketName;

    private JFrame frame;


    public S3TransferSampleModel () {
    	
    	try {
			getCredentials();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
    	buildTransferManager();
    	bucketName = "treefrog-transfer-test-akiaiqq5r75nzrr7j6xq";
        //bucketName = "treefrog-transfer-test-" + credentials.getAWSAccessKeyId().toLowerCase();
        //createAmazonS3Bucket();
    }
    
    public void transferFile (File fileToUpload, ProgressListener listener) {
    	buildObjectRequest (fileToUpload, listener);
    }
    
    private void buildTransferManager() {
    	
        AmazonS3 s3 = new AmazonS3Client(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        s3.setRegion(usWest2);
        tx = new TransferManager(s3);
        
    }
    
    private void buildObjectRequest (File fileToUpload, ProgressListener listener) {
    	
        PutObjectRequest request = new PutObjectRequest(
                bucketName, fileToUpload.getName(), fileToUpload)
            .withGeneralProgressListener (listener);
        Upload upload = tx.upload(request);
       
    }
         
    private void getCredentials() throws Exception {
    	
        try {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (/home/joel/.aws/credentials), and is in valid format.",
                    e);
        }        
    }    

    private void createAmazonS3Bucket() {
        try {
            if (tx.getAmazonS3Client().doesBucketExist(bucketName) == false) {
                tx.getAmazonS3Client().createBucket(bucketName);
            }
        } catch (AmazonClientException ace) {
            JOptionPane.showMessageDialog(frame, "Unable to create a new Amazon S3 bucket: " + ace.getMessage(),
                    "Error Creating Bucket", JOptionPane.ERROR_MESSAGE);
        }
    }
}
