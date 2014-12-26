package com.buddyware.treefrog.filesystem.model;

import com.buddyware.treefrog.filesystem.model.local.BufferedFileSystem;
import com.buddyware.treefrog.filesystem.model.local.LocalFileSystem;

public class FileSystemFactory {

	public static FileSystem buildFileSystem(
										FileSystemType type, String rootPath) {
		
		FileSystem fs = null;
		
		switch (type) {
		
		case SOURCE_DISK:
		case LOCAL_DISK:
			fs = new BufferedFileSystem(type, rootPath);
			break;
			
		case AMAZON_S3:
			break;
			
		default:
			break;
		}
		
		return fs;
	}
}
