package com.buddyware.treefrog;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.buddyware.treefrog.util.TaskMessage;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;

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
