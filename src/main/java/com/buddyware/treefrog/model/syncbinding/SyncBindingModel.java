package com.buddyware.treefrog.model.syncbinding;

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
import com.buddyware.treefrog.model.filesystem.FileSystem;
import com.buddyware.treefrog.model.filesystem.FileSystemModel;
import com.buddyware.treefrog.model.filesystem.FileSystemProperty;
import com.buddyware.treefrog.model.filesystem.FileSystemType;
import com.buddyware.treefrog.model.syncbinding.SyncBinding.SyncFlag;

public class SyncBindingModel extends BaseModel {

	// hashtable containing bindings
	private final List<SyncBinding> mBindings = new ArrayList<SyncBinding>();

	private final EnumSet<SyncFlag> mFullSync = 
			EnumSet.of(	SyncFlag.SYNC_ADD_FILES,
						SyncFlag.SYNC_CHANGED_FILES,
						SyncFlag.SYNC_REMOVE_FILES,
						SyncFlag.SYNC_TO_SOURCE,
						SyncFlag.SYNC_TO_TARGET);
	/*
	 * Class constructor
	 */
	public SyncBindingModel() {

	};
	
	public void addBinding (List <Pair <String, String>> props,
							FileSystem source,
							FileSystem target) {
		
		if (source == null || target == null)
			return;
		
		createBinding(source, target, mFullSync);
	}
	
	public void updateBinding (List <Pair <String, String>> props) {
	
		String id = null;
		
		//get the name of the binding that was updated
		for (Pair <String, String> prop: props) {

			if (prop.getKey().equals( 
					SyncBindingProperty.ID.toString())) {
		
				id = prop.getValue();
				break;
			}
		}
	
		//if not found, abort.  Invalid update
		if (id == null)
			return;
		
		SyncBinding binding = null;

		for (SyncBinding b: mBindings) {

			if (b.getId().equals(id)) {
				binding = b;
				break;
			}
		}
	
		//no binding?  no update
		if (binding == null)
			return;
				
		//parse the remaining properties
		for (Pair <String, String> prop: props) {
			binding.setProperty(
					SyncBindingProperty.valueOf(prop.getKey()),
					prop.getValue());
		}			
		
	}
	
	public void serialize(IniFile iniFile) {
		
		for (SyncBinding binding: mBindings) {

			iniFile.putData(binding.getId(), "OBJECT", "SYNCBINDING");
		
			for (int i = 0; i < FileSystemProperty.values().length; i++) {

				SyncBindingProperty prop = SyncBindingProperty.values()[i];
				
				iniFile.putData (binding.getId(), prop.toString(), binding.getProperty(prop));
			
			}
		}
	
		iniFile.write();
	}
	
	public List<SyncBinding> values() { return mBindings; }
	
	public ListChangeListener <Pair <String, String> > getUpdateListener () {
		return new ListChangeListener <Pair <String, String> > () {

			@Override
			public void onChanged(Change c) {
				//TODO:  Add update code here
			}
		};
	}
	
	public void deserialize(IniFile iniFile, FileSystemModel filesystems) {
				

		/*
		 * Loads the filesystems.cfg file (creating if it does not exist)
		 * and them populates the model with the defined bindings
		 */

		for (String id: iniFile.getEntries().keySet()) {

			Map <String, String> props = iniFile.getEntries().get(id);

			if (props == null)
				continue;

			if (!props.get("OBJECT").equals("SYNCBINDING"))
				continue;
			
			String sourceId = null;
			String targetId = null;
			
			for (String propName: props.keySet()) {
				
				SyncBindingProperty prop =
						SyncBindingProperty.valueOf(propName);
				
				if (prop == SyncBindingProperty.SOURCE)
					sourceId = props.get(propName);
				
				if (prop == SyncBindingProperty.TARGET)
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
	
	public SyncBinding createBinding (FileSystem source, FileSystem target,
			EnumSet<SyncFlag> syncFlags) {
	
		// do not add if a binding for this source / target pair already exists
		for (SyncBinding sb: mBindings) {
			
			if (sb.getBindSourceId().equals(source.getId()) && 
				sb.getBindTargetId().equals(target.getId()))
					return sb;
		}	
	
		SyncBinding binding = new SyncBinding(source, target, syncFlags);
		mBindings.add(binding);

		return binding;
	}
	
	public List <SyncBinding> bindings() { return mBindings; }

}
