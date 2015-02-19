package com.buddyware.treefrog.syncbinding.model;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Map;

import com.buddyware.treefrog.BaseModel;
import com.buddyware.treefrog.filesystem.FileSystemType;
import com.buddyware.treefrog.filesystem.FileSystemsModel;
import com.buddyware.treefrog.filesystem.model.FileSystemModel;
import com.buddyware.treefrog.syncbinding.model.SyncBinding.SyncFlag;
import com.buddyware.treefrog.util.IniFile;
import com.buddyware.treefrog.util.utils;

public class SyncBindingModel extends BaseModel {

	// hashtable containing bindings
	private final Hashtable<String, SyncBinding> mBindingTable = new Hashtable<String, SyncBinding>();

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
	
	public void serialize(IniFile iniFile) {
				
	}
	
	public void deserialize(IniFile iniFile, FileSystemsModel filesystems) {
				
		//get the key name (filesystem name), and retrieve the corresponding map
		//of key-value property pairs.  Then create file systems based on 
		//the data in the config file
		for (String bnd_name: iniFile.getEntries().keySet()) {

			if (bnd_name.startsWith("node"))
					continue;
			
			Map <String, String> bnd_props = iniFile.getEntries().get(bnd_name);
			
			if (bnd_props == null)
				continue;
				
			String[] bnd_targets = bnd_name.split("\\.");
			
			if (bnd_targets.length != 3)
				continue;
			
			 
			//build the model defined in the config file
			bindFileSystems(filesystems.getFileSystem(bnd_targets[1]),
							filesystems.getFileSystem(bnd_targets[2]), 
							mFullSync
							);
		}		
	}
	public SyncBinding createBinding (FileSystemModel source, FileSystemModel target,
			EnumSet<SyncFlag> syncFlags) {
		
		SyncBinding binding = bindFileSystems(source, target, syncFlags);
		
		//TODO: replace with notification to property that a binding has been created 
		//serialize();
		return binding;
	}
	
	private SyncBinding bindFileSystems(FileSystemModel source, FileSystemModel target,
			EnumSet<SyncFlag> syncFlags) {

		String bindingKey = source.toString() + "." + target.toString();

		// do not add if a binding for this source / target pair already exists
		if (mBindingTable.containsKey(bindingKey))
			return null;

		SyncBinding binding = new SyncBinding(source, target, syncFlags);
		mBindingTable.put(bindingKey, binding);

		return binding;
	}

}
