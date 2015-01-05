package com.buddyware.treefrog.syncbinding.model;

import java.util.EnumSet;
import java.util.Hashtable;

import com.buddyware.treefrog.BaseModel;
import com.buddyware.treefrog.filesystem.model.FileSystem;
import com.buddyware.treefrog.syncbinding.model.SyncBinding.SyncFlag;

public class SyncBindingModel extends BaseModel {

	// hashtable containing bindings
	private final Hashtable<String, SyncBinding> mBindingTable = new Hashtable<String, SyncBinding>();

	/*
	 * Class constructor
	 */
	public SyncBindingModel() {

	};

	public void bindFilesystems(FileSystem source, FileSystem target,
			EnumSet<SyncFlag> syncFlags) {

		String bindingKey = source.toString() + "." + target.toString();

		// do not add if a binding for this source / target pair already exists
		if (mBindingTable.containsKey(bindingKey))
			return;

		SyncBinding binding = new SyncBinding(source, target, syncFlags);
		mBindingTable.put(bindingKey, binding);
	}

}
