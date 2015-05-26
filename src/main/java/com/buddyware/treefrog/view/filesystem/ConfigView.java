package com.buddyware.treefrog.view.filesystem;

import java.io.IOException;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;

public interface ConfigView {

	public void serialize() throws IOException;
	public Node node();
	public void addUpdateListener (ChangeListener listener);
}
