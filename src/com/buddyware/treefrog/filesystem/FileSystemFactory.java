package com.buddyware.treefrog.filesystem;

import com.buddyware.treefrog.filesystem.local.model.LocalFileSystem;

public class FileSystemFactory {

	public static FileSystem buildFileSystem(
										FileSystemType type, String rootPath) {
		
		FileSystem fs = null;
		
		switch (type) {
		
		case LOCAL_DISK:
			fs = new LocalFileSystem(rootPath);
			break;
			
		case AMAZON_S3:
			break;
			
		default:
			break;
		}
		
		return fs;
	}
}
