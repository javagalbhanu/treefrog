package com.buddyware.treefrog.filesystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.collections.ListChangeListener;
import javafx.geometry.Point2D;
import javafx.util.Pair;

import com.buddyware.treefrog.BaseModel;
import com.buddyware.treefrog.filesystem.model.FileSystemModel;
import com.buddyware.treefrog.filesystem.model.FileSystemModelProperty;
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
	
	public void updateModel (List <Pair <String, String>> props) {
				
		String id = null;
		
		//get the name of the model that was updated
		for (Pair <String, String> prop: props) {

			if (prop.getKey().equals( 
					FileSystemModelProperty.ID.toString())) {
				
				System.out.println(prop.getKey());			
				id = prop.getValue();
				break;
			}
		}
	
		//if not found, abort.  Invalid update
		if (id == null)
			return;
		
		FileSystemModel model = null;

		for (FileSystemModel m: mModels) {

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
					FileSystemModelProperty.valueOf(prop.getKey()),
					prop.getValue());
		}	
	}

	public FileSystemModel addModel (List <Pair<String, String>> props) {
		
		FileSystemType fs_type = null;

		for (Pair <String, String> prop: props) {
			
			if (prop.getKey().equals(FileSystemModelProperty.TYPE.toString()))
				fs_type = FileSystemType.valueOf(prop.getValue());
		}

		//no type?  no model
		if (fs_type == null)
			return null;
		
		FileSystemModel model = buildFileSystem(fs_type, null);
				
		for (Pair <String, String> prop: props) {
		
			FileSystemModelProperty propKey = 
					FileSystemModelProperty.valueOf(prop.getKey());
			
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
					FileSystemType.valueOf(fs_props.get(FileSystemModelProperty.TYPE.toString()));
			
			if (fs_type == null)
				continue;
			
			String fs_path =
					fs_props.get(FileSystemModelProperty.PATH.toString());
						
			FileSystemModel model = buildFileSystem(fs_type, fs_path);
					
			for (String propName: fs_props.keySet()) {
			
				FileSystemModelProperty prop = 
						FileSystemModelProperty.valueOf(propName);
				
				model.setProperty(prop, fs_props.get(propName));
			}
			
			mModels.add(model);
		}
	}	
	
	public List <FileSystemModel> getFileSystemsByType (FileSystemType fs_type) {
		
		List <FileSystemModel> models = new ArrayList <FileSystemModel> ();
		
		for (FileSystemModel model: mModels)
			if (model.getType().equals(fs_type))
				models.add(model);
		
		return models;
	}
	
	public List <FileSystemModel> fileSystems () { return mModels; }
	
	public FileSystemModel getFileSystem(String id) {
	
		for (FileSystemModel model: mModels)
			if (model.getId().equals(id))
				return model;
		
		return null;
	}
	
	public void serialize(IniFile iniFile) {
		
		for (FileSystemModel model: mModels) {

			iniFile.putData(model.getId(), "OBJECT", "FILESYSTEM");
		
			for (int i = 0; i < FileSystemModelProperty.values().length; i++) {

				FileSystemModelProperty prop = FileSystemModelProperty.values()[i];
				
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
	
	public List <FileSystemModel> values() { return mModels; }
	
	private FileSystemModel buildFileSystem(FileSystemType type,
			String rootPath) {

		FileSystemModel fs = null;
		String name = type.toString();
		
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
