package com.buddyware.treefrog.util;

import java.nio.file.Paths;
import java.util.prefs.Preferences;

public class ApplicationPreferences {

	private final Preferences mPreferences;

	public enum PreferenceId {
		ROOT_PATH, AMAZON_S3_CREDENTIAL_PATH
	}

	public ApplicationPreferences() {

		mPreferences = Preferences.userRoot().node(this.getClass().getName());
	}

	public String get(PreferenceId prefId) {

		String defaultValue = "";

		switch (prefId) {

		case ROOT_PATH:
			defaultValue = Paths.get(
					System.getProperty("user.home") + "/bucketsync").toString();
			break;
		default:
			break;
		}

		return mPreferences.get(prefId.toString(), defaultValue);
	}

	public void set(PreferenceId prefId, String value) {
		mPreferences.put(prefId.toString(), value);
	}
}
