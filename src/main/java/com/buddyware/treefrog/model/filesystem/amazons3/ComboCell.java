package com.buddyware.treefrog.model.filesystem.amazons3;

import java.util.Map;

import javafx.scene.control.ListCell;

public class ComboCell extends ListCell<Map <String, String>> {

	private final Map <String, Map <String, String> > mModel;
	
	public ComboCell(Map<String, Map<String, String> > model) {
	
		mModel = model;
	}
}
