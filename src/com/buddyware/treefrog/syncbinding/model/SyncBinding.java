package com.buddyware.treefrog.syncbinding.model;

import java.util.EnumSet;

import javafx.collections.ListChangeListener;

import com.buddyware.treefrog.filesystem.model.FileSystem;
import com.buddyware.treefrog.filesystem.model.SyncPath;
import com.buddyware.treefrog.filesystem.model.SyncType;

public class SyncBinding {
/*
 * Provides active synchronization between a source and target file model
 * 
 */
	
	private final static String TAG = "SyncBinding";
	
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
	
		source.pathsChanged().addListener( createFileSystemChangeListener 
										(target, source));
	}
	
	private final ListChangeListener<SyncPath> createFileSystemChangeListener(
			FileSystem source, FileSystem target) {
				return new ListChangeListener<SyncPath>() {

					@Override
					public void onChanged(javafx.collections.ListChangeListener
						.Change<? extends SyncPath> arg0) {

							if (!target.isStartingUp()) {

								for (SyncPath filepath: arg0.getList()) {
									
									//skip if no file is defined
									if (filepath.getFile() == null)
										continue;
									
									switch (filepath.getSyncType()) {
									
									case SYNC_CREATE:
										source.putFile(filepath);										
									break;
										
									case SYNC_MODIFY:
									break;
										
									case SYNC_DELETE:
										System.out.println ("Delete success? " + source.deleteFile(filepath));
									break;
										
									default:
									break;
									}
								}
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
