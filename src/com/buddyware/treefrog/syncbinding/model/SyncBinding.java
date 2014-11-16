package com.buddyware.treefrog.syncbinding.model;

import java.util.EnumSet;

import javafx.collections.ListChangeListener;

import com.buddyware.treefrog.filesystem.FileSystem;

public class SyncBinding {
/*
 * Provides active synchronization between a source and target file model
 * 
 */
	
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
	
	//active sync flags for the binding
	private EnumSet<SyncFlag> mSyncFlags;
	
	public SyncBinding (FileSystem source, FileSystem target,
						EnumSet<SyncFlag> syncFlags) {
		
		//create listeners which correspond to a default synchronization of
		//full mirror (two-way add/change/remove updates)
		
		source.addedPaths().addListener( createFileSystemChangeListener 
										(source, target));

		source.changedPaths().addListener( createFileSystemChangeListener 
										(source, target));

		source.removedPaths().addListener( createFileSystemChangeListener 
										(source, target));

		target.addedPaths().addListener( createFileSystemChangeListener 
										(target, source));

		target.changedPaths().addListener( createFileSystemChangeListener 
										(target, source));

		target.removedPaths().addListener( createFileSystemChangeListener 
										(target, source));		
	}
	
	private final ListChangeListener<String> createFileSystemChangeListener(
			FileSystem source, FileSystem target) {
				return new ListChangeListener<String>() {

					@Override
					public void onChanged(javafx.collections.ListChangeListener
						.Change<? extends String> arg0) {
							for (String filepath: arg0.getList()) {
								source.putFile(target.getFile(filepath));
							}
					}
				};
	}	
	
	//Synchronization settings can be provided using class static profiles
	//or by specifying custom sync flags
	public void setSyncFlags(EnumSet<SyncFlag> syncflags) {
		mSyncFlags = syncflags;
	}	
}
