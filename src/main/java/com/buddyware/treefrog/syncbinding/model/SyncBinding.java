package com.buddyware.treefrog.syncbinding.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javafx.collections.ListChangeListener;

import com.buddyware.treefrog.BaseModel;
import com.buddyware.treefrog.filesystem.model.FileSystemModel;
import com.buddyware.treefrog.filesystem.model.FileSystemModelProperty;
import com.buddyware.treefrog.filesystem.model.SyncPath;

public class SyncBinding extends BaseModel {
	/*
	 * Provides active synchronization between a source and target file model
	 */

	private final static String TAG = "SyncBinding";
	private String mBindSourceId;
	private String mBindTargetId;

	/*
	 * SyncFlags provide fine-grained control over synchronization by direction
	 * (sync to source / target) and type of update (sync files as they are
	 * added, removed, and/or changed)
	 */
	public enum SyncFlag {
		SYNC_TO_SOURCE, SYNC_TO_TARGET, SYNC_ADD_FILES, SYNC_REMOVE_FILES, SYNC_CHANGED_FILES
	}

	/*
	 * SyncProfiles provide an encapsulation of combinations of SyncFlags
	 */
	public static final EnumSet<SyncFlag> SYNC_PROFILE_FULL = EnumSet
			.allOf(SyncFlag.class);

	// active sync flags for the binding
	private EnumSet<SyncFlag> mSyncFlags;

	public String getBindSourceId() { return mBindSourceId; }
	public String getBindTargetId() { return mBindTargetId; }
	
	public SyncBinding(FileSystemModel bindSource, FileSystemModel bindTarget,
			EnumSet<SyncFlag> syncFlags) {

		// create listeners which correspond to a default synchronization of
		// full mirror (two-way add/change/remove updates)

		mBindSourceId = bindSource.getId();
		mBindTargetId = bindTarget.getId();
System.out.println("Binding source / target: "  + mBindSourceId + ", " + mBindTargetId);		
		bindTarget.pathsChanged().addListener(
				createFileSystemChangeListener(bindSource, bindTarget));

		bindSource.pathsChanged().addListener(
				createFileSystemChangeListener(bindTarget, bindSource));
	}

	private final ListChangeListener<SyncPath> createFileSystemChangeListener(
			FileSystemModel target, FileSystemModel source) {

		return new ListChangeListener<SyncPath>() {

			private final List<SyncPath> mCacheFiles = new ArrayList<SyncPath>();
			private final List<SyncPath> mDeleteFiles = new ArrayList<SyncPath>();

			@Override
			public final synchronized void onChanged(
					javafx.collections.ListChangeListener.Change<? extends SyncPath> arg0) {

				// skip synchronizations that result from the initial
				// pathfinding

				if (!mCacheFiles.isEmpty())
					mCacheFiles.clear();

				if (!mDeleteFiles.isEmpty())
					mDeleteFiles.clear();

				for (SyncPath filepath : arg0.getList()) {

					// skip if no file is defined
					if (filepath.getFile() == null)
						continue;

					String pathname = filepath.getRelativePath().toString();

					switch (filepath.getSyncType()) {

					case SYNC_CREATE:
						System.out.println("\n" + TAG + ".SYNC_CREATE\n\t"
								+ target.getRootPath() + "\n\t\t"
								+ filepath.getPath());
						mCacheFiles.add(filepath);
						break;

					case SYNC_MODIFY:
						System.out.println("\n" + TAG + ".SYNC_MODIFY\n\t"
								+ target.getRootPath() + "\n\t\t"
								+ filepath.getPath());
						mCacheFiles.add(filepath);
						break;

					case SYNC_DELETE:
						System.out.println("\n" + TAG + ".SYNC_DELETE\n\t"
								+ target.getRootPath() + "\n\t\t"
								+ filepath.getPath());
						mDeleteFiles.add(filepath);
						break;

					default:
						break;
					}
				}

				if (!mCacheFiles.isEmpty()) {
					target.putFiles(mCacheFiles);
					mCacheFiles.clear();
				}

				if (!mDeleteFiles.isEmpty()) {
					target.deleteFiles(mDeleteFiles);
					mDeleteFiles.clear();
				}
			}
		};
	}

	// Synchronization settings can be provided using class static profiles
	// or by specifying custom sync flags
	public void setSyncFlags(EnumSet<SyncFlag> syncflags) {
		mSyncFlags = syncflags;
	}
	
	public void setProperty (SyncBindingProperty propName, String value) {
		
		switch(propName) {

		case ID:
			setId (value);
		break;
		
		case SOURCE:
		break;
		
		case TARGET:
		break;
		/*
		case LAYOUT_X:
			setLayoutPosition (Double.valueOf(value), mLayoutPoint.getY());
		break;
		
		case LAYOUT_Y:
			setLayoutPosition (mLayoutPoint.getX(), Double.valueOf(value));
		break;
			*/	
		default:
		break;
		}
	}	
	
	public String getProperty (SyncBindingProperty propName) {
		
		switch (propName) {
			
		case ID:
			return getId();
			
		case SOURCE:
			return getBindSourceId();
		
		case TARGET:
			return getBindTargetId();
/* 
		case NAME:
			return getName();
			
		case LAYOUT_X:
			return Double.toString(getLayoutPoint().getX());
			
		case LAYOUT_Y:
			return Double.toString(getLayoutPoint().getY());
			
		case TYPE:
			return getType().toString();

		case PATH:
			
			Path p = getRootPath();
			
			if (p == null)
				return "";

			return p.toString();
		*/
		default:
			break;
		}
		
		return null;
	}
		
}
