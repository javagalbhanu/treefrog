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
		alert.setContentText("\n\nDo you wish to let Bucketsync automatically create it's own access key?");

		ButtonType buttonAuto = new ButtonType("Yes, let Bucketsync do it.");
		ButtonType buttonManual = new ButtonType("Thanks, but I'll do it myself.");
		
		alert.getButtonTypes().setAll(buttonAuto, buttonManual);
		
		Optional <ButtonType> result = alert.showAndWait();

	
		return (result.get() == buttonAuto);
	}

	private void showCredentialHelp() {
		Alert alert = new Alert(AlertType.INFORMATION);
		
		alert.setTitle("Bucketsync Help:  Amzaon S3");
		alert.setHeaderText("Root Credentials and User Access Keys");
		alert.setContentText("Root Credentials give complete control of the account."
				+ "\nYou should not use root credentials not be used for everyday access."
				+"\n\nBucketsync can automatically create user credentials to access your account,"
				+"but it must use your root credentials to do so."
				+"\n\nOtherwise, you must create these credentials in your Amazon AWS console by going to:"
				+"\n\nServices -> Administration & Security");
		alert.showAndWait();
	}

	public void showInvalidCredentials() {
		Alert alert = new Alert(AlertType.INFORMATION);
		
		alert.setTitle("Invalid Credentials");
		alert.setHeaderText("Could not create bucketsync user credentials.");
		alert.setContentText("The credentials were either not found \nor do not have sufficient permissions."
				+ "\n\nEnsure the user exists, has the AdministratorAccess user policy attached and try again.");
				
		alert.showAndWait();
	}

	public void alertMissingRootCredentials () {

		Alert alert = new Alert (AlertType.ERROR);
		alert.setTitle("Root Credential Error");
		alert.setHeaderText("Unable to retrieve root credentials");
		alert.setContentText("Root credentials are typically stored in a file called 'credentials.txt'" +
							"\n\nIf you do not have root credentials, you will need to retrieve them" +
							"\nfrom your Amazon AWS account.");

		alert.showAndWait();
		
		return;
	}
	
	public void alertMissingCredentials () {

		Alert alert = new Alert (AlertType.ERROR);
		alert.setTitle("Credential Error");
		alert.setHeaderText("Unable to retrieve credentials");
		alert.setContentText(".bucketsync/credentials.cfg\n\nmay be missing or corrupted.");

		alert.showAndWait();
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