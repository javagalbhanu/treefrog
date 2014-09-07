package com.buddyware.treefrog;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.buddyware.treefrog.util.TaskMessage;
import com.buddyware.treefrog.util.TaskMessage.TaskMessageType;

import javafx.concurrent.Task;

public class BaseTask extends Task<Void> {
	
	protected final BlockingQueue <TaskMessage> messageQueue;	
	
	protected BaseTask (BlockingQueue <TaskMessage> messageQueue) {
			this.messageQueue = messageQueue;
	};
	
    public void enqueueMessage (String message, TaskMessageType messageType) {
    	try {
    		messageQueue.put(new TaskMessage (message, messageType));
    	} catch (InterruptedException e) {
    		e.printStackTrace();
    	}    	
    };
    	
	@Override
	protected Void call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected ExecutorService createExecutor(final String name, boolean isDaemon) {
		 
		 ThreadFactory factory = new ThreadFactory() {
			 
			 @Override public Thread newThread(Runnable r) {
				 Thread t = new Thread(r);
				 t.setName(name);
				 t.setDaemon(isDaemon);
				 return t;
			 }
		 };
		 
		 return Executors.newSingleThreadExecutor(factory);
	} 	
	
}
