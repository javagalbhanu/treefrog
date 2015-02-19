package com.buddyware.treefrog.filesystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.geometry.Point2D;

import com.buddyware.treefrog.BaseController;
import com.buddyware.treefrog.BaseModel;
import com.buddyware.treefrog.filesystem.model.FileSystemModel;
import com.buddyware.treefrog.filesystem.model.local.BufferedFileSystem;
import com.buddyware.treefrog.util.IniFile;
import com.buddyware.treefrog.util.utils;

public class FileSystemsModel extends BaseModel {
/*
 * Serves to create / delete and manage existing file systems
 * and provide serialization / deserialization services 
 */

	List <FileSystemModel> mModels = new ArrayList <FileSystemModel> ();
	
	public FileSystemsModel() {}
	
	public void deserialize(IniFile iniFile) {
		
		/*
		 * Loads the filesystems.cfg file (creating if it does not exist)
		 * and them populates the model with the defined filesystems
		 */
		
		//get the key name (filesystem name), and retrieve the corresponding map
		//of key-value property pairs.  Then create file systems based on 
		//the data in the config file
		for (String fs_name: iniFile.getEntries().keySet()) {

			if (fs_name.startsWith("binding"))
					continue;
			
			Map <String, String> fs_props = iniFile.getEntries().get(fs_name);
			
			if (fs_props == null)
				continue;
				
			FileSystemType fs_type = FileSystemType.valueOf(fs_props.get("type"));
			String fs_path = fs_props.get("path");

			if (fs_type == null)
				continue;
			
			if (fs_path == null)
				continue;
			
			if (fs_name == null)
				continue;
			
			//build the model defined in the config file
			createModel(fs_type, fs_path, fs_name.substring(fs_name.indexOf(".")+1));
		}
	}	
	
	public List <FileSystemModel> getFileSystemsByType (FileSystemType fs_type) {
		
		List <FileSystemModel> models = new ArrayList <FileSystemModel> ();
		
		for (FileSystemModel model: mModels)
			if (model.getType().equals(fs_type))
				models.add(model);
		
		return models;
	}
	
	public FileSystemModel getFileSystem(String name) {
	
		for (FileSystemModel model: mModels)
			if (model.getName().equals(name))
				return model;
		
		return null;
	}
	
	public FileSystemModel createModel (FileSystemType fs_type, String root_path, String name) {
		
		FileSystemModel model = buildFileSystem(fs_type, root_path);
		
		if (name != null)
			model.setName(name);
		
		mModels.add(model);
		
		model.start();
		
		return model;
	}
	
	public void serialize(IniFile iniFile) {
		
		for (FileSystemModel model: mModels) {
		
			String fs_name = "node." + model.getName();
			Point2D fs_layout = model.getLayoutPoint();
			
			iniFile.putData(fs_name, "type", model.getType().toString());
			iniFile.putData(fs_name, "path", model.getRootPath().toString());
			iniFile.putData(fs_name, "layoutX", Double.toString(fs_layout.getX()));
			iniFile.putData(fs_name, "layoutY", Double.toString(fs_layout.getY()));
			
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
	
	public List <FileSystemModel> values() { return mModels; }
	
	private FileSystemModel buildFileSystem(FileSystemType type,
			String rootPath) {

		FileSystemModel fs = null;
		String name = "untitled_"; // + BaseController.mMain.fileSystems().count();
		
		switch (type) {

		case SOURCE_DISK:
			name = utils.getHostName();
			
		case LOCAL_DISK:
			fs = new BufferedFileSystem(type, rootPath);
			fs.setName(name);
			break;

		case AMAZON_S3:
			break;

		default:
			break;
		}

		return fs;
	}	
	
	public void removeModel (FileSystemModel file_system) {
		
		for (FileSystemModel model: mModels) {
			
			if (model.equals(file_system)) {
				mModels.remove(model);
				break;
			}
		}
	}
}
