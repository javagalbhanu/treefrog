package com.buddyware.treefrog.model.filesystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;

import com.buddyware.treefrog.BaseModel;
import com.buddyware.treefrog.model.IniFile;
import com.buddyware.treefrog.model.filesystem.amazons3.AmazonS3FileSystem;
import com.buddyware.treefrog.model.filesystem.local.BufferedFileSystem;

public class FileSystemModel extends BaseModel {
/*
 * Serves to create / delete and manage existing file systems
 * and provide serialization / deserialization services 
 */

	List <FileSystem> mModels = new ArrayList <FileSystem> ();
	
	public FileSystemModel() {}
	
	public void updateModel (List <Pair <String, String>> props) {
				
		String id = null;
		
		//get the name of the model that was updated
		for (Pair <String, String> prop: props) {

			if (prop.getKey().equals( 
					FileSystemProperty.ID.toString())) {
	
				id = prop.getValue();
				break;
			}
		}
	
		//if not found, abort.  Invalid update
		if (id == null)
			return;
		
		FileSystem model = null;

		for (FileSystem m: mModels) {

			if (m.getId().equals(id)) {
				model = m;
				break;
			}
		}
	
		//no model?  no update
		if (model == null)
			return;
				
		//parse the remaining properties
		for (Pair <String, String> prop: props) {
			model.setProperty(
					FileSystemProperty.valueOf(prop.getKey()),
					prop.getValue());
		}	
	}

	public FileSystem addModel (List <Pair<String, String>> props) {
		
		FileSystemType fs_type = null;

		for (Pair <String, String> prop: props) {
			
			if (prop.getKey().equals(FileSystemProperty.TYPE.toString()))
				fs_type = FileSystemType.valueOf(prop.getValue());
		}

		//no type?  no model
		if (fs_type == null)
			return null;
		
		FileSystem model = buildFileSystem(fs_type, null);
		
		for (Pair <String, String> prop: props) {
		
			FileSystemProperty propKey = 
					FileSystemProperty.valueOf(prop.getKey());
			
			model.setProperty(propKey, prop.getValue());
		}
		
		mModels.add(model);
		
		return model;
	}	
	
	public void deserialize(IniFile iniFile) {
		
		/*
		 * Loads the filesystems.cfg file (creating if it does not exist)
		 * and them populates the model with the defined filesystems
		 */

		for (String fs_id: iniFile.getEntries().keySet()) {

			Map <String, String> fs_props = iniFile.getEntries().get(fs_id);

			if (fs_props == null)
				continue;

			if (!fs_props.get("OBJECT").equals("FILESYSTEM"))
				continue;
			
			FileSystemType fs_type = 
					FileSystemType.valueOf(fs_props.get(FileSystemProperty.TYPE.toString()));
			
			if (fs_type == null)
				continue;
			
			String fs_path =
					fs_props.get(FileSystemProperty.PATH.toString());
						
			FileSystem model = buildFileSystem(fs_type, fs_path);
					
			for (String propName: fs_props.keySet()) {
			
				FileSystemProperty prop = 
						FileSystemProperty.valueOf(propName);
				
				model.setProperty(prop, fs_props.get(propName));
			}
			
			mModels.add(model);
		}
	}	
	
	public List <FileSystem> getFileSystemsByType (FileSystemType fs_type) {
		
		List <FileSystem> models = new ArrayList <FileSystem> ();
		
		for (FileSystem model: mModels)
			if (model.getType().equals(fs_type))
				models.add(model);
		
		return models;
	}
	
	public List <FileSystem> fileSystems () { return mModels; }
	
	public FileSystem getFileSystem(String id) {
	
		for (FileSystem model: mModels)
			if (model.getId().equals(id))
				return model;
		
		return null;
	}
	
	public void serialize(IniFile iniFile) {
		
		for (FileSystem model: mModels) {

			iniFile.putData(model.getId(), "OBJECT", "FILESYSTEM");
		
			for (int i = 0; i < FileSystemProperty.values().length; i++) {

				FileSystemProperty prop = FileSystemProperty.values()[i];
				
				iniFile.putData (model.getId(), prop.toString(), model.getProperty(prop));
			}
		}
	
		iniFile.write();
	}
	
	public void shutdown() {

		while (mModels.size() > 0) {
			mModels.get(0).shutdown();
			mModels.remove(0);
		}
	}
	
	public Integer count() { return mModels.size(); }
	
	public List <FileSystem> values() { return mModels; }
	
	private FileSystem buildFileSystem(FileSystemType type,
			String rootPath) {

		FileSystem fs = null;
		String name = type.toString();
		
		switch (type) {

		case LOCAL_DISK:
			fs = new BufferedFileSystem(type, rootPath);
			fs.setName(name);
			break;

		case AMAZON_S3:
			
			fs = new AmazonS3FileSystem(rootPath);
			fs.setName(name);
			break;

		default:
			break;
		}

		return fs;
	}	
	
	public void removeModel (FileSystem file_system) {
		
		for (FileSystem model: mModels) {
			
			if (model.equals(file_system)) {
				mModels.remove(model);
				break;
			}
		}
	}
}
