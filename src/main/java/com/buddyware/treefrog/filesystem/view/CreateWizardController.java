package com.buddyware.treefrog.filesystem.view;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

import com.buddyware.treefrog.util.utils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class CreateWizardController {

	private final Deque<Pane> mPastPages = new ArrayDeque<Pane>();

	@FXML
	StackPane wizardStackPane;

	@FXML
	ComboBox comboSyncTargets;

	@FXML
	Button buttonSyncToCloud;

	@FXML
	Button buttonSyncToComputer;

	@FXML
	Button buttonLeft;

	@FXML
	Button buttonMiddle;

	@FXML
	Button buttonRight;

	@FXML
	Button buttonTest;

	@FXML
	TextField textUsername;

	@FXML
	TextField textPassword;

	@FXML
	Pane page_one;
	@FXML
	Pane page_two;
	@FXML
	Pane page_three;

	private final static String BACK_LABEL = "< Back";
	private final static String NEXT_LABEL = "Next >";
	private final static String FINISH_LABEL = "Finish";

	/**
	 * FXML initialization requirement
	 */
	@FXML
	private void initialize() {
		// Listen for TextField text changes
		textUsername.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				updateButtonTestState();
			}
		});

		textPassword.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {

				updateButtonTestState();
			}

		});
	}

	private void updateButtonTestState() {

		Boolean unEmpty = textUsername.getText().isEmpty();
		Boolean pwEmpty = textPassword.getText().isEmpty();

		buttonTest.setDisable(unEmpty || pwEmpty);
	}

	@FXML
	public void onComboSyncTargetAction() {
		buttonMiddle.setText(NEXT_LABEL);
		buttonLeft.setVisible(true);
		buttonMiddle.setVisible(true);
	}

	@FXML
	public void nextPage() {

		int pageCount = wizardStackPane.getChildren().size();

		if (pageCount == 0)
			return;

		Pane page = (Pane) wizardStackPane.getChildren().remove(pageCount - 1);

		mPastPages.push(page);

		if (buttonLeft.isVisible())
			buttonLeft.setVisible(false);

		if (!buttonMiddle.isVisible())
			buttonMiddle.setVisible(true);

		if (!buttonMiddle.getText().equals(BACK_LABEL))
			buttonMiddle.setText(BACK_LABEL);

		if (buttonMiddle.getText().equals(NEXT_LABEL)) {

			if (wizardStackPane.getChildren().size() == 1)
				buttonMiddle.setText(FINISH_LABEL);
		}
	}

	@FXML
	public void backPage() {

		int pageCount = mPastPages.size();

		if (pageCount == 0)
			return;

		wizardStackPane.getChildren().add(mPastPages.pop());

		if (mPastPages.size() > 0) {
			buttonMiddle.setVisible(true);
			buttonMiddle.setText(NEXT_LABEL);
			buttonLeft.setVisible(true);
		} else {
			buttonLeft.setVisible(false);
			buttonMiddle.setVisible(false);
		}
	}

	private void initComboBox(Collection list) {

		comboSyncTargets.getItems().clear();

		// comboSyncTargets.setPlaceHolder("No valid destinations found...");
		comboSyncTargets.getItems().addAll(list);
		comboSyncTargets.setPromptText("Select...");
		comboSyncTargets.getSelectionModel().clearSelection();
		comboSyncTargets.setValue(null);
		// comboSyncTargets.getSelectionModel().select(0);
	}

	@FXML
	public void middleButtonClicked() {

		switch (buttonMiddle.getText()) {

		case BACK_LABEL:
			backPage();
			break;

		case NEXT_LABEL:
			nextPage();
			break;

		case FINISH_LABEL:
			// build
		}

	}

	private void setupPageTwo(boolean localSetup) {

		if (!localSetup) {
			initComboBox(FXCollections.observableArrayList(utils
					.getRemoteProviders()));
		} else {

			ArrayList<String> volList = new ArrayList<String>();

			for (File path : utils.getVolumes()) {
				volList.add(path.toString());
			}

			initComboBox(FXCollections.observableArrayList(volList));
		}
	}

	private void setupPageThree(boolean localSetup) {

		buttonTest.setVisible(!localSetup);
		page_three.setVisible(!localSetup);

	}

	private void setupPageFour(boolean localSetup) {

	}

	private void setupPages(boolean localSetup) {

		setupPageTwo(localSetup);
		setupPageThree(localSetup);
		setupPageFour(localSetup);
	}

	@FXML
	public void chooseRemote() {

		setupPages(false);
		nextPage();

	}

	@FXML
	public void chooseLocal() {

		setupPages(true);
		nextPage();

	}
}
