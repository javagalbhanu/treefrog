package com.buddyware.treefrog.model.binding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.util.Pair;

import com.buddyware.treefrog.BaseModel;
import com.buddyware.treefrog.utils;
import com.buddyware.treefrog.model.IniFile;
import com.buddyware.treefrog.model.binding.BindingModel.SyncFlag;
import com.buddyware.treefrog.model.filesystem.FileSystemModel;
import com.buddyware.treefrog.model.filesystem.FileSystemCollection;
import com.buddyware.treefrog.model.filesystem.FileSystemProperty;
import com.buddyware.treefrog.model.filesystem.FileSystemType;

public class BindingCollection extends BaseModel {

	// hashtable containing bindings
	private final List<BindingModel> mBindings = new ArrayList<BindingModel>();

	private final EnumSet<SyncFlag> mFullSync = 
			EnumSet.of(	SyncFlag.SYNC_ADD_FILES,
						SyncFlag.SYNC_CHANGED_FILES,
						SyncFlag.SYNC_REMOVE_FILES,
						SyncFlag.SYNC_TO_SOURCE,
						SyncFlag.SYNC_TO_TARGET);
	/*
	 * Class constructor
	 */
	public BindingCollection() {};
	
	public BindingModel addBinding (List <Pair <String, String>> props,
							FileSystemModel source, FileSystemModel target) {
		
		if (source == null || target == null)
			return null;
		
		return createBinding(source, target, mFullSync);
	}
	
	public void updateBinding (List <Pair <String, String>> props) {
	
		String id = null;
		
		//get the name of the binding that was updated
		for (Pair <String, String> prop: props) {

			if (!prop.getKey().equals(BindingView.ID.toString()))
				continue;
			
			id = prop.getValue();
			break;
		}
	
		//if not found, abort.  Invalid update
		if (id == null)
			return;
		
		BindingModel binding = null;

		for (BindingModel b: mBindings) {

			if (!b.getId().equals(id))
				continue;
			
				binding = b;
				break;
		}
	
		//no binding?  no update
		if (binding == null)
			return;
				
		//parse the remaining properties
		for (Pair <String, String> prop: props) {
			binding.setProperty(
					BindingView.valueOf(prop.getKey()),
					prop.getValue());
		}			
		
	}
	
	public void serialize(IniFile iniFile) throws IOException {
		
		for (BindingModel binding: mBindings)
			binding.serialize(iniFile);
	}
	
	public List<BindingModel> values() { return mBindings; }
	
	public ListChangeListener <Pair <String, String> > getUpdateListener () {
		return new ListChangeListener <Pair <String, String> > () {

			@Override
			public void onChanged(Change c) {
				//TODO:  Add update code here
			}
		};
	}
	
	public void deserialize(FileSystemCollection filesystems) {
				

		/*
		 * Loads the filesystems.cfg file (creating if it does not exist)
		 * and them populates the model with the defined bindings
		 */
		
		IniFile iniFile = null;
		
		try {
			iniFile = new IniFile(utils.getFileSystemConfigPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (String id: iniFile.select().keySet()) {

			Map <String, String> props = iniFile.select().get(id);

			if (props == null)
				continue;

			if (!props.get("OBJECT").equals("SYNCBINDING"))
				continue;
			
			String sourceId = null;
			String targetId = null;
			
			for (String propName: props.keySet()) {
				
				BindingView prop =
						BindingView.valueOf(propName);
				
				if (prop == BindingView.SOURCE)
					sourceId = props.get(propName);
				
				if (prop == BindingView.TARGET)
					targetId = props.get(propName);
				
			}
			
			if (sourceId == null || targetId == null)
				continue;
			
			
			createBinding (filesystems.getFileSystem(sourceId),
							filesystems.getFileSystem(targetId), 
							mFullSync
							);
		}		
	}
	
	public BindingModel createBinding (FileSystemModel source, FileSystemModel target,
			EnumSet<SyncFlag> syncFlags) {
	
		// do not add if a binding for this source / target pair already exists
		for (BindingModel sb: mBindings) {
			
			if (sb.getBindSourceId().equals(source.getId()) && 
				sb.getBindTargetId().equals(target.getId()))
					return sb;
		}	
	
		BindingModel binding = new BindingModel(source, target, syncFlags);
		mBindings.add(binding);

		return binding;
	}
	
	public List <BindingModel> bindings() { return mBindings; }

}
