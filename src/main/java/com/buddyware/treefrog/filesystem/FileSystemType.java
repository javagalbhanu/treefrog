package com.buddyware.treefrog.filesystem;

public enum FileSystemType {
	SOURCE_DISK, // The source file system
	LOCAL_DISK, // A file system local to the client
	AMAZON_S3, // Amazon S3 cloud
	UNDEFINED
}
