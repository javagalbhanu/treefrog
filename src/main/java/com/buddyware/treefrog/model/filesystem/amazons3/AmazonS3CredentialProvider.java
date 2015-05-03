package com.buddyware.treefrog.model.filesystem.amazons3;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.auth.profile.internal.Profile;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AccessKey;
import com.amazonaws.services.identitymanagement.model.CreateAccessKeyRequest;
import com.amazonaws.services.identitymanagement.model.CreateAccessKeyResult;
import com.buddyware.treefrog.utils;

public class AmazonS3CredentialProvider {
/*
 * Provides data for the available access keys on a user's system across multiple sources:
 *  - Java Application Preferences
 *  - System Environment Variables
 *  - Credentials File
 */
	
	private final List <Path> mCredentialPaths = new ArrayList <Path> ();
	private final Map <String, AWSCredentials> mAwsCredentials = 
									new HashMap <String, AWSCredentials> ();
	
	public AmazonS3CredentialProvider () {
		discoverCredentials();
	}
	
	private void discoverCredentials() {
		/*
		 * Uses the three AWS credential providers to lookup keys in each of the three locations.
		 * Discovered keys are stored in a map under the location names:
		 * "SysEnvVar", "JavaPref", and profile names declared in .aws/credentials
		 */
		
		AWSCredentials creds = getEnvironmentVariableCredentials();
		
		if (creds != null)
			mAwsCredentials.put("SysEnvVar", creds);
		
		creds = getSystemPropertyCredentials();
		
		if (creds != null)
			mAwsCredentials.put("JavaPref", creds);
		

		Map <String, AWSCredentials> credProfiles = getConfigFileCredentials(null);
		
		//load profiles defined in the credentials file if it exists
		if (credProfiles != null)
			mAwsCredentials.putAll(getConfigFileCredentials(null));
		
		/*TODO:
		 * Need to check for existence of credential profiles for bucketsync AWS user accounts
		 * in the appdata path.  If the file doesn't exist, don't load anything - defer creation
		 * until an AWS account is assigned to bucket sync
		 * 
		 * Also need to write housekeeping functions that create the appdata folder for bucketsync
		 * and default config files...
		 */

		//load bucketsync-specific profiles saved in the application data path, if any
		//bucketsync profiles saved as:
		//bucketsync.[source].[target]
		//credProfiles = getConfigFileCredentials(utils.getApplicationDataPath() + "/bucketsync_credentials");
		
	//	if (credProfiles != null)
		//	mAwsCredentials.putAll(credProfiles);
	}
	
	public Map <String, AWSCredentials> getCredentials() {
		return mAwsCredentials;
	}
	
	public Set<String> getCredentialProfiles() {
		return mAwsCredentials.keySet();
	}
	
	public Map <String, AWSCredentials> getConfigFileCredentials(String filepath) {
		
		Map <String, Profile> profiles = null;
		
		ProfilesConfigFile provider;
		
		if (filepath == null)
			provider = new ProfilesConfigFile();
		else
			provider = new ProfilesConfigFile(filepath);
		
		try {
			profiles = provider.getAllProfiles();
		} catch (AmazonClientException e) {
			return null;
		}
		
		Map <String, AWSCredentials> creds = new HashMap <String, AWSCredentials> ();
		
		for (Profile profile: profiles.values()) {
			creds.put(profile.getProfileName(), profile.getCredentials());
		
	}
		return creds;
	}
	
	public AWSCredentials getEnvironmentVariableCredentials() {
		
		EnvironmentVariableCredentialsProvider provider = 
				new EnvironmentVariableCredentialsProvider();
		
		AWSCredentials result = null;
		
		try {
			result = provider.getCredentials();
		} catch (AmazonClientException e) {
			return null;
		}

		return result;
	}
	
	public AWSCredentials getSystemPropertyCredentials() {
		
		SystemPropertiesCredentialsProvider provider = 
				new SystemPropertiesCredentialsProvider();
		
		AWSCredentials result;
		
		try {
			result = provider.getCredentials();
		} catch (AmazonClientException e) {
			return null;
		}

		return result;
	}
	
	public AccessKey testCreateUser (String profileName) {
		
		AmazonIdentityManagementClient aimc = 
				new AmazonIdentityManagementClient(mAwsCredentials.get(profileName));
		
		CreateAccessKeyRequest keyRequest = new CreateAccessKeyRequest("bucketsync");
		
		return aimc.createAccessKey(keyRequest).getAccessKey();		
	}
}
