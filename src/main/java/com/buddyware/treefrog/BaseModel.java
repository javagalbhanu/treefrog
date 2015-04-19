package com.buddyware.treefrog;

import java.util.UUID;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public abstract class BaseModel {

	private String mName;
	
	private String mId = UUID.randomUUID().toString();

	protected ExecutorService createExecutor(final String name, boolean isDaemon) {

		ThreadFactory factory = new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName(name);
				t.setDaemon(isDaemon);
				return t;
			}
		};

		return Executors.newSingleThreadExecutor(factory);
	}
	
	public void setId (String id) { mId = id; }
	public String getId() { return mId; }
	
	public String getName() { return mName; }
	public void setName(String name) { mName = name; }
	
	public void serialize(String filepath) {
		
	}
	
	public void deserialize(String filepath) {
		
	}
}
