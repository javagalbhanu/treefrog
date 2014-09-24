package com.buddyware.treefrog;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.Stage;

public class BaseController {

		public static Main mMain;
		
		protected Stage parentStage;
		
		private final StringProperty errorMessage = new SimpleStringProperty();
		private final StringProperty activityMessage = new SimpleStringProperty();
		
		public void setParentStage(Stage parent) {
			parentStage = parent;
		}
		
		public StringProperty errorMessage() { return errorMessage; }	
		public StringProperty activityMessage() { return activityMessage; }
		
		public void addErrorListener(StringProperty remoteProperty) {

			remoteProperty.addListener(new ChangeListener<String>(){
				@Override
				public void changed(ObservableValue<? extends String> arg0,
						String arg1, String arg2) {
		             errorMessage.setValue((String) arg0.getValue());
				}
		      });
		}
		
		public void addActivityListener(StringProperty remoteProperty) {

			remoteProperty.addListener(new ChangeListener<String>(){
				@Override
				public void changed(ObservableValue<? extends String> arg0,
						String arg1, String arg2) {
		             activityMessage.setValue((String) arg0.getValue());
				}
		      });
		}		
}
