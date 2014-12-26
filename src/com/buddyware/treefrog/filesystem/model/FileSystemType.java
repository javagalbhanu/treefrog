package com.buddyware.treefrog.filesystem.model;

public enum FileSystemType {
	SOURCE_DISK,	//The source file system
	LOCAL_DISK,		//A file system local to the client
	BUFFERED_DISK,
	AMAZON_S3,		//Amazon S3 cloud
	FILE_SYSTEM		//Generic file system??
}
