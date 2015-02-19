package com.buddyware.treefrog.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IniFile {

	//Map of sections and sub-map of key-value pairs
   private final Map< String, Map <String, String > >  mEntries  = new HashMap <> ();

   private final String mPath;
   
   public IniFile( String path ) throws IOException {
	   
	   mPath = path;
	   
	   load( null );
   }

   public Map< String, Map <String, String> > getEntries() { return mEntries; }
   
   public void putData (String section, String key, String value) {
	
	   if (section == null || key == null || value == null)
		   return;
	   
	   if (section.isEmpty() || key.isEmpty() || value.isEmpty())
		   return;

	   Map <String, String> pairs = mEntries.get(section);
	   
	   if (pairs == null) {
		   
		   pairs = new HashMap <String, String> ();
		   mEntries.put(section, pairs);
		   
	   }
	   
	   pairs.put(key, value);
   }
   
   public void load (String path) throws IOException {
	
	   if (path == null)
		   path = mPath;
	   
	   File fil = new File (path);
	   
	   //if the file does not exist, create a new one
	   if (!fil.exists()) {
		   fil.getParentFile().mkdirs(); 
		   fil.createNewFile();
	   }
	   
	   try (BufferedReader br = 
	    		  new BufferedReader( new FileReader( path ))) {
		   
		   String line;
		   String section = null;
		   Map <String, String> pairs = null;
		   
		   while ((line = br.readLine()) != null) {
			   
			   //skip comment lines
			   if (line.startsWith("#"))
				   continue;
			   
			  //if this isn't a key/value pair, check for section header.
			  if (!line.contains("=")) {
				  
				  //if this is a section header, get the header text and start a new pairs map
				  if (line.startsWith("[")) {
					  
					  	String new_section = line.substring(line.indexOf("["), line.indexOf("]")).trim();
				  		pairs = new HashMap <String, String> ();
					  
				  		//if the header text is valid, write the previous pairs map to the entries map
				  		//under the previous section
					  if (new_section != null) {
						  if (!new_section.isEmpty()) {
							  
							  //save only non-empty pairs
							  if (pairs != null)
								  if (!pairs.isEmpty())
									  mEntries.put(section, pairs);
							  
							  section = new_section;
						  }
					  }
				  }
				continue;
			  }
			  
			  int eqIdx = line.indexOf("=");
			  
			  //this is a key/value pair.  Save it if the key is non-zls
			  String key = line.substring(0, eqIdx).trim();
			  String value = line.substring(eqIdx + 1).trim();
			  
			  if (pairs != null)
				  if (!key.isEmpty())
					  pairs.put(key, value);			  
		   }
	   }
   }
   
   public void write() {
	   write (mPath);
   }
   
   public void write (String path) {
	   
	   if (path == null)
		   path = mPath;
	   
	   if (mEntries.size() == 0)
		   return;
	   
	   try (BufferedWriter br = 
			   new BufferedWriter( new FileWriter( path ))) {
		   
		   for (String section: mEntries.keySet()) {
			   
			   br.write("[" + section.trim() + "]");
			   br.newLine();
		   
			   for (String key:mEntries.get(section).keySet()) {
				   br.write(key + "=" + mEntries.get(section).get(key));
				   br.newLine();
			   }
			   br.newLine();
		   }

		   br.close();
		   
	   } catch (IOException e) {
		e.printStackTrace();
	}
   }
}