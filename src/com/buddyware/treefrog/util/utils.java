package com.buddyware.treefrog.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.Main;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class utils {
	
	public final static String userHome = System.getProperty("user.home");
	public final static Path exlusionsFilepath = Paths.get(userHome + "/exclusions.cfg");

	public static void appendToFile (Path file, String data) {
		
		ArrayList <String> dataList = new ArrayList<String>();
		
		dataList.add (data);
		
		appendToFile (file, dataList);
	}
	
	public static void appendToFile (Path file, List<String> data) {
	
		try {
			Files.write(file, data, StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			
		} catch (IOException e){
			e.printStackTrace();
		}
		
	}
	
	public static <S extends Pane> Stage createDialogStage 
						(String resource, Modality modality, Stage parent) {

		try {
		    // Load the fxml file and create a new stage for the popup dialog.
		    FXMLLoader loader = new FXMLLoader();
		    loader.setLocation(Main.class.getResource(resource));
		    S page = (S) loader.load();
		    
			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			//dialogStage.setTitle(title);
			dialogStage.initModality(modality);
			dialogStage.initOwner(parent);
			Scene scene = new Scene (page);
			dialogStage.setScene (scene);

		return dialogStage;
		
		} catch (IOException e) {
		    e.printStackTrace();
		    return null;
		}
	}

	public static <S extends Pane> S loadFxml 
								(String resource, BaseController controller) {
		
			//creates a new fxml object, returning the created layout
			
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation (Main.class.getResource(resource));
			S layout = null;
			
			try {
				layout = (S) fxmlLoader.load();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

			if (controller != null)
				fxmlLoader.setController(controller);
			
			return layout;
	}
	
	public static <S extends Pane, T extends BaseController> T loadFxml 
	(String resource, Stage parentStage, BaseController controller) {
		
		//creates a new fxml object, returning the controller (if assigned)
		//parent stage contains the created scene and it's root layout of the generic type
			
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation (Main.class.getResource(resource));
			S layout = null;
			
			try {
				layout = (S) fxmlLoader.load();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			//scene containing root layout
			Scene sc = new Scene (layout);

			parentStage.setScene (sc);
			parentStage.show();
		
			if (controller != null)
				fxmlLoader.setController(controller);
			else {
				if (fxmlLoader.getController() instanceof BaseController) {
					controller = fxmlLoader.getController();
					controller.setParentStage(parentStage);
				}
				else
					controller = null;
			}
			
			return (T) controller;
	};
	
	public static String[] getRemoteProviders() {
		
		String[] list = {"Amazon S3"};
		return list;
	}
	
	public static File[] getVolumes() {
		
		switch(OpSys.getType()) {
		
		case OS_OSX:
			return getOsxVolumes();
			
		case OS_WINDOWS:
			return getWindowsVolumes();
			
		case OS_LINUX:
			return getLinuxVolumes();
		}
		
		return null;
	}
	
	private static File[] getOsxVolumes() {
		return new File("/Volumes").listFiles();		
	}
	
	private static File[] getWindowsVolumes() {
		return File.listRoots();		
	}
	
	private static File[] getLinuxVolumes() {
		
		ArrayList<File> files = new ArrayList<File>();
		FileInputStream stream = null;
		Scanner scanner = null;
		
		try {
			
			stream = new FileInputStream("/etc/mtab");
			scanner = new Scanner (stream, "UTF-8");
			
		    while (scanner.hasNextLine()) {
		    	
		    	String line = scanner.nextLine();
		    	
		    	//if the line starts with a forward slash, it's likely a
		    	//valid device.  Look for the next forward slash following a space
		    	//and get everything that follows up to the third space
		    	
		    	if (line.startsWith("/")) {
		    		int startIdx = line.indexOf("/",line.indexOf(" "));
		    		int endIdx = line.indexOf(" ", startIdx);
		    		files.add( Paths.get(line.substring(startIdx, endIdx)).toFile());
  		
		    	}

		    }
		    
		    // note that Scanner suppresses exceptions
		    if (scanner.ioException() != null) {
		        throw scanner.ioException();
		    }
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} finally {
			if (stream != null)
				try {
					stream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		    if (scanner != null)
		        scanner.close();
		}		
		
		return files.toArray(new File[files.size()]);
	}
}