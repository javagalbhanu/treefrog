package com.buddyware.treefrog.local.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;


public final class LocalFileModelScanner {

	private FileInputStream inputStream = null;
	private Scanner scanner = null;
	private final HashMap <String, String> map = new HashMap <String, String> ();
	
	public LocalFileModelScanner (Path exlusionsfilepath) {

		String[] arr;
		
		try {
			
			inputStream = new FileInputStream (exlusionsfilepath.toString());
			scanner = new Scanner (inputStream, "UTF-8");
			String[] record = new String[2];
			
		    while (scanner.hasNextLine()) {
		    	
		    	String line = scanner.nextLine();
		    	
		    	if (line.contains(","))
		    		record = line.split(",");
		    	else
		    		record[0] = line;
		    	System.out.println ("Loading exlusion: " + record[0] + " - " + record [1]);		    	
		    	map.put(record[0], record[1]);
		    }
		    
		    // note that Scanner suppresses exceptions
		    if (scanner.ioException() != null) {
		        throw scanner.ioException();
		    }
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} finally {
		    if (inputStream != null)
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		    if (scanner != null)
		        scanner.close();
		}		
	}
	
	public HashMap<String, String> getStreamMap() {
		
		return map;
	}
}