package com.buddyware.treefrog.model.filesystem.amazons3;

import java.util.HashMap;
import java.util.Map;

public class CredentialItem {

	private Map <String, String> mProps;
	
	public CredentialItem ( String AccessKeyId, String SecretAccessKey,
							String ProfileName, String CredentialRotation) {
	
		mProps = new HashMap<String, String> ();
		
		mProps.put(AmazonS3Property.ACCESS_KEY_ID.toString(), AccessKeyId);
		mProps.put(AmazonS3Property.SECRET_ACCESS_KEY.toString(), SecretAccessKey);
		mProps.put(AmazonS3Property.CREDENTIAL_ROTATION.toString(), CredentialRotation);
		mProps.put(AmazonS3Property.PROFILE_NAME.toString(), ProfileName);
	}
	
	public CredentialItem (Map <String, String> props) {
		mProps = props;
		
		if (mProps == null)
			mProps = new HashMap <String, String>();
	}
	
	public Map <String, String> getMap() { return (mProps); }
	
	public String toString() { return getProfileName(); }
		
	public String getAccessKeyId() { 
		return mProps.get(AmazonS3Property.ACCESS_KEY_ID.toString()); }

	public String getSecretAccessKey() {
		return mProps.get(AmazonS3Property.SECRET_ACCESS_KEY.toString()); }
	
	public String getCredentialRotation() {
		return mProps.get(AmazonS3Property.CREDENTIAL_ROTATION.toString()); }
	
	public String getProfileName() {
		return mProps.get(AmazonS3Property.PROFILE_NAME.toString()); }
}
