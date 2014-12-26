package com.buddyware.treefrog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javafx.concurrent.Task;

public class BaseTask<T> extends Task<T> {

	protected BaseTask () {
	};
    	
	@Override
	protected T call() throws Exception {
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
