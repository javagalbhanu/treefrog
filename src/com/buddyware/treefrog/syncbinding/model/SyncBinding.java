package com.buddyware.treefrog.syncbinding.model;

import java.util.EnumSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.beans.property.SimpleListProperty;
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
	
	public SyncBinding (FileSystem bindSource, FileSystem bindTarget,
						EnumSet<SyncFlag> syncFlags) {
		
		//create listeners which correspond to a default synchronization of
		//full mirror (two-way add/change/remove updates)

		bindTarget.pathsChanged().addListener( createFileSystemChangeListener 
				(bindSource, bindTarget));
		
		bindSource.pathsChanged().addListener( createFileSystemChangeListener 
				(bindTarget, bindSource));
		
		
	}
	
	private final ListChangeListener<SyncPath> createFileSystemChangeListener(
			FileSystem target, FileSystem source) {
		
				return new ListChangeListener<SyncPath>() {

					@Override
					public final synchronized void onChanged(javafx.collections.ListChangeListener
						.Change<? extends SyncPath> arg0) {

							//skip synchronizations that result from the initial
							//pathfinding
						
							if (source.isStartingUp())
								return;
							
							for (SyncPath filepath: arg0.getList()) {
								
								//skip if no file is defined
								if (filepath.getFile() == null)
									continue;
								
								String pathname = filepath.getRelativePath().toString();
								
								switch (filepath.getSyncType()) {
								
								case SYNC_CREATE:
									System.out.println ("\n" + TAG + ".SYNC_CREATE\n\t" + target.getRootPath() + "\n\t\t" + filepath.getPath());									
									target.cacheFile(filepath);										
								break;
									
								case SYNC_MODIFY:
									System.out.println ("\n" + TAG + ".SYNC_MODIFY\n\t" + target.getRootPath() + "\n\t\t" + filepath.getPath());
									target.cacheFile(filepath);
								break;
									
								case SYNC_DELETE:
									System.out.println ("\n" + TAG + ".SYNC_DELETE\n\t" + target.getRootPath() + "\n\t\t" + filepath.getPath());									
									target.deleteFile(filepath);
								break;
									
								default:
								break;
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
