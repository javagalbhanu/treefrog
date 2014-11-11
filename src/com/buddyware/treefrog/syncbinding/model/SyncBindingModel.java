package com.buddyware.treefrog.syncbinding.model;

import java.util.EnumSet;

import com.buddyware.treefrog.BaseModel;
import com.buddyware.treefrog.filesystem.IFileSystem;

public class SyncBindingModel extends BaseModel {

	/*
	 * SyncFlags provide fine-grained control over synchronization by
	 * direction (sync to source / target) and type of update (sync files
	 * as they are added, removed, and/or changed)
	 */
	public enum SyncFlag {
		SYNC_TO_SOURCE,
		SYNC_TO_TARGET,
		SYNC_ADD_FILES,
		SYNC_REMOVE_FILES,
		SYNC_CHANGED_FILES
	}
	
	/*
	 * SyncProfiles provide an encapsulation of combinations of SyncFlags
	 */
	public static final EnumSet<SyncFlag> SYNC_PROFILE_FULL = 
												EnumSet.allOf(SyncFlag.class);
	

	//source / target file systems for the binding
	private final IFileSystem mSourceFileSystem;
	private final IFileSystem mTargetFileSystem;
	
	//active sync flags for the binding
	private EnumSet<SyncFlag> mSyncFlags;
	
	
	/*
	 * Class constructor
	 */
	public SyncBindingModel (IFileSystem source, IFileSystem target) {
		mSourceFileSystem = source;
		mTargetFileSystem = target;
		
		//TODO:  Attach listeners to the filesystems.  This may need to be
		//delayed until synchronization options are set, or perhaps default to
		//full synchronization and remove listeners if a lesser synchronization
		//is later specified.
	}
	
	//Synchronization settings can be provided using class static profiles
	//or by specifying custom sync flags
	public void setSyncFlags(EnumSet<SyncFlag> syncflags) {
		mSyncFlags = syncflags;
	}
}
