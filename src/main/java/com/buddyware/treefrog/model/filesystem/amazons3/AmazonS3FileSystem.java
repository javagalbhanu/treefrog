package com.buddyware.treefrog.model.filesystem.amazons3;

import java.nio.file.Path;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.buddyware.treefrog.model.filesystem.FileSystem;
import com.buddyware.treefrog.model.filesystem.FileSystemType;
import com.buddyware.treefrog.model.filesystem.SyncPath;

public class AmazonS3FileSystem extends FileSystem {

	private AWSCredentials credentials = null;
	private TransferManager tx;
	private String bucketName;

	private AmazonS3 s3;

	public AmazonS3FileSystem(String rootPath) {
		super(FileSystemType.AMAZON_S3, rootPath);
		construct();
	}

	@Override
	protected void construct() {

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public Path getFile(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteFiles(List<SyncPath> paths) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * public AmazonS3FileSystem () { try { getCredentials();
	 * 
	 * 
	 * } catch (Exception e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 * 
	 * s3 = new AmazonS3Client(credentials); buildTransferManager();
	 * 
	 * bucketName = "foo4you";
	 * 
	 * }
	 * 
	 * 
	 * 
	 * public void transferFile (String bucketName, File fileToUpload,
	 * ProgressListener listener) { PutObjectRequest request = new
	 * PutObjectRequest(bucketName, fileToUpload.getName(), fileToUpload)
	 * .withGeneralProgressListener (listener);
	 * 
	 * Upload upload = tx.upload(request); }
	 * 
	 * private void buildTransferManager() {
	 * 
	 * 
	 * Region usWest2 = Region.getRegion(Regions.US_WEST_2);
	 * s3.setRegion(usWest2); tx = new TransferManager(s3);
	 * 
	 * }
	 * 
	 * private void getCredentials() throws Exception {
	 * 
	 * try { credentials = new
	 * ProfileCredentialsProvider("default").getCredentials(); } catch
	 * (Exception e) { throw new AmazonClientException(
	 * "Cannot load the credentials from the credential profiles file. " +
	 * "Please make sure that your credentials file is at the correct " +
	 * "location (/home/joel/.aws/credentials), and is in valid format.", e); }
	 * }
	 * 
	 * private void createAmazonS3Bucket() { try { if
	 * (tx.getAmazonS3Client().doesBucketExist(bucketName) == false) {
	 * tx.getAmazonS3Client().createBucket(bucketName); } } catch
	 * (AmazonClientException ace) { // JOptionPane.showMessageDialog(frame,
	 * "Unable to create a new Amazon S3 bucket: " + ace.getMessage(), //
	 * "Error Creating Bucket", JOptionPane.ERROR_MESSAGE); } } public ArrayList
	 * <String> ListBuckets() { List <Bucket> bucketlist = s3.listBuckets();
	 * ArrayList <String> results = new ArrayList <String> ();
	 * 
	 * 
	 * // ArrayList <String> results = new ArrayList <String> ();
	 * 
	 * for (Bucket b: bucketlist) { results.add(b.getName()); }
	 * 
	 * return results; }
	 */
}
