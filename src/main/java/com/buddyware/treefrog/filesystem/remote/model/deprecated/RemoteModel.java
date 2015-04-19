package com.buddyware.treefrog.filesystem.remote.model.deprecated;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javafx.collections.ObservableList;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.identitymanagement.*;
import com.amazonaws.services.identitymanagement.model.*;

public class RemoteModel {

	private AWSCredentials credentials = null;
	private TransferManager tx;
	private String bucketName;

	private AmazonS3 s3;

	public RemoteModel() {
		try {
			getCredentials();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		s3 = new AmazonS3Client(credentials);
		buildTransferManager();

		bucketName = "foo4you";

	}

	public void transferFile(String bucketName, File fileToUpload,
			ProgressListener listener) {
		PutObjectRequest request = new PutObjectRequest(bucketName,
				fileToUpload.getName(), fileToUpload)
				.withGeneralProgressListener(listener);

		Upload upload = tx.upload(request);
	}

	private void buildTransferManager() {

		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		s3.setRegion(usWest2);
		tx = new TransferManager(s3);

	}

	private void getCredentials() throws Exception {

		try {
			credentials = new ProfileCredentialsProvider("default")
					.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. "
							+ "Please make sure that your credentials file is at the correct "
							+ "location (/home/joel/.aws/credentials), and is in valid format.",
					e);
		}
	}

	private void createAmazonS3Bucket() {
		try {
			if (tx.getAmazonS3Client().doesBucketExist(bucketName) == false) {
				tx.getAmazonS3Client().createBucket(bucketName);
			}
		} catch (AmazonClientException ace) {
			// JOptionPane.showMessageDialog(frame,
			// "Unable to create a new Amazon S3 bucket: " + ace.getMessage(),
			// "Error Creating Bucket", JOptionPane.ERROR_MESSAGE);
		}
	}

	public ArrayList<String> ListBuckets() {
		List<Bucket> bucketlist = s3.listBuckets();
		ArrayList<String> results = new ArrayList<String>();

		// ArrayList <String> results = new ArrayList <String> ();

		for (Bucket b : bucketlist) {
			results.add(b.getName());
		}

		return results;
	}

}
