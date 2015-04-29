package com.buddyware.treefrog.view.filesystem.amazons3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;

public class CredentialWizard {

	public boolean chooseAutoSetup() {
		
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Amazon S3 Credential Wizard");
		alert.setHeaderText("IAM User Access credentials must be provided before continuing.");
		alert.setContentText("Bucketsync can do this, using your root credentials for your Amazon S3 account."
				+ "\n\nTypically, these credentials are stored on your local system in a file called credentials.txt."
				+ "\nIf you prefer to provide IAM User Access credentials yourself, you must log in to your Amazon AWS console,"
				+ "\ngo to Services -> Administration & Security -> IAM to create a new user access key."
				+"\n\nDo you wish to let Bucketsync automatically create it's own access key?");
		
		ButtonType buttonHuh = new ButtonType("What are root credentials and access keys?");
		ButtonType buttonAuto = new ButtonType("Yes, let Bucketsync do the hard part.");
		ButtonType buttonManual = new ButtonType("Thanks, but I'd rather do it myself.");
		
		alert.getButtonTypes().setAll(buttonHuh, buttonAuto, buttonManual);
		
		Optional <ButtonType> result = alert.showAndWait();
		
		if (result.get() == buttonHuh)
			showCredentialHelp();
	
		return (result.get() == buttonAuto);
	}

	private void showCredentialHelp() {
		Alert alert = new Alert(AlertType.INFORMATION);
		
		alert.setTitle("Bucketsync Help:  Amzaon S3");
		alert.setHeaderText("Root Credentials and User Access Keys");
		alert.setContentText("Credentials are simply passwords Amazon S3 uses to protect your data."
				+ "\nRoot Credentials are credentials that give the owner complete control of the account."
				+ "\nLog in to your Amazon AWS console and visit Services -> Administration & Security to learn more."
				+ "\n\nIAM User Access Keys are keys which grant acess to your account.  However, unlike your"
				+ "\nroot credentials, user access keys have restricted permissions which you may specify."
				+ "\n\nBucketSync will use your root credentials to generate a limited User Access key to"
				+ "\nprovide it access to your account.  This key can be managed or deleted directly by"
				+"\nlogging in to your Amazon AWS console.");
	}

	public void alertMissingCredentials () {
		
		Alert alert = new Alert (AlertType.ERROR);
		alert.setTitle("Credential Error");
		alert.setHeaderText("Unable to retrieve credentials");
		alert.setContentText("The file .bucketsync/credentials.cfg may be missing or corrupted.");
		
	}
	
	public String chooseRootCredentialProfile(Set<String> profiles) {
		
		List<String> choices = new ArrayList();
		
		for (String p: profiles)
			choices.add(p);
		
		ChoiceDialog<String> dialog = new ChoiceDialog<>("",choices);
		
		dialog.setTitle("Select a Credential Profile");
		dialog.setHeaderText("The following profile(s) were found.");
		dialog.setContentText("Profile");
		
		Optional<String> result = dialog.showAndWait();
		
		if (result.isPresent())
			return result.get();
		
		return null;
	}
}