package com.buddyware.treefrog;

import java.util.ArrayList;
import java.util.List;
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

	protected final BlockingQueue<TaskMessage> taskMessages = new ArrayBlockingQueue(
			1);

	public ArrayList<TaskMessage> pollMessages() {

		ArrayList<TaskMessage> messages = new ArrayList<TaskMessage>();
		int messageCount = taskMessages.drainTo(messages);

		return messages;
	};

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
}
