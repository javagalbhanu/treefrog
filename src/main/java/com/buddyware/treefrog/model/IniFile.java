package com.buddyware.treefrog.model;

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
   private BufferedWriter mBufferWriter;
   
   public IniFile( String path ) throws IOException {
	   
	   mPath = path;
	   
	   load();
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
   
   public void load() throws IOException {
	   
	   File fil = new File (mPath);
  
	   //if the file does not exist, create a new one
	   if (!fil.exists()) {
		   fil.getParentFile().mkdirs(); 
		   fil.createNewFile();
	   }
	   
	   try (BufferedReader br = 
	    		  new BufferedReader( new FileReader( mPath ))) {
		   
		   String line;
		   String section = null;
		   Map <String, String> pairs = new HashMap <String, String> ();
		   
		   while ((line = br.readLine()) != null) {

			   //skip comment lines
			   if (line.startsWith("#"))
				   continue;
			   
			  //if this isn't a key/value pair, check for section header.
			  if (!line.contains("=")) {
				  
				  //if this is a section header, get the header text and start a new pairs map
				  if (line.startsWith("[")) {

					  String new_section = line.substring(line.indexOf("[") + 1, line.indexOf("]")).trim();
					  
					  if (new_section == null)
						  continue;
					  
					  if (new_section.isEmpty())
						  continue;
					  
					  //if the header text is valid, write the previous pairs map to the entries map
					  //under the previous section
					  
					  if (!pairs.isEmpty()) {
						  mEntries.put(section, pairs);
						  pairs = new HashMap <String, String> ();
					  }
					  
					  section = new_section;
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
		   
		   //write the last occurring entry
		   if (pairs != null)
			   if (!pairs.isEmpty())
				   mEntries.put(section, pairs);
	   }
   }
   
   public void open() {
	   try {
		   mBufferWriter = new BufferedWriter( new FileWriter( mPath ));
	   } catch (IOException e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
	   }
   }
   
   public void close() {
	   try {
		   mBufferWriter.close();
	   } catch (IOException e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
	   }
	   mBufferWriter = null;
   }
   
   public void write() {
	   
	   if (mEntries.size() == 0)
		   return;
	   
	   //open / close the file with the write, or leave it open?
	   //if the bufferwriter is valid (file is already open)
	   //then don't close it when finished
	   boolean leaveOpen = !(mBufferWriter == null);
	   
	   if (!leaveOpen)
		   open();
	   
	   try (BufferedWriter br = 
			   new BufferedWriter( new FileWriter( mPath ))) {
		   
		   for (String section: mEntries.keySet()) {
			   
			   br.write("[" + section.trim() + "]");
			   br.newLine();
		   
			   for (String key:mEntries.get(section).keySet()) {
				   br.write(key + "=" + mEntries.get(section).get(key));
				   br.newLine();
			   }
			   br.newLine();
		   }

		   if (!leaveOpen)
			   close();
		   
	   } catch (IOException e) {
		e.printStackTrace();
	}
   }
}