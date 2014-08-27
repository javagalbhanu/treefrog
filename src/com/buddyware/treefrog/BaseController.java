package com.buddyware.treefrog;

import javafx.stage.Stage;

public class BaseController {

		public static Main mMain;
		
		private Stage mParent;
		
		public void setParentStage(Stage parent) {
			mParent = parent;
		}
}
