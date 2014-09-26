package com.buddyware.treefrog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class BaseService extends Service <Void> {

    private final BlockingQueue<String> activityBlockingQueue = new ArrayBlockingQueue<> (1);
    private final BlockingQueue<String> errorBlockingQueue = new ArrayBlockingQueue<> (1);
    
    private final List<String> activityQueue = new ArrayList<String>();
	private final List<String> errorQueue = new ArrayList<String>();
	
	private final IntegerProperty errorFlag = new SimpleIntegerProperty (this, "int", 0);
	private final IntegerProperty activityFlag = new SimpleIntegerProperty (this, "int", 0);
    
    protected final Boolean isBlocking = true;
    
	@Override
	protected Task<Void> createTask() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setBlocking (Boolean flag) {
		
		if (flag == isBlocking)
			return;
		
		activityBlockingQueue.clear();
		errorBlockingQueue.clear();

		activityBlockingQueue.poll();
		errorBlockingQueue.poll();

		activityQueue.clear();
		errorQueue.clear();
		
	}
	
	public void putActivityMessage (String message) {
	
		if (isBlocking) {
			System.out.println ("Putting activity message " + message + isBlocking);
			System.out.println ("queue hash: " + System.identityHashCode(activityBlockingQueue));
			activityBlockingQueue.add(message);
			
		}
		else
			activityQueue.add(message);
		
		activityFlag.set(activityFlag.get() + 1);
	}
	
	public void putErrorMessage (String message) {

		if (isBlocking)
			errorBlockingQueue.add(message);
		else
			errorQueue.add(message);
		
		errorFlag.set(activityFlag.get() + 1);
	}
	
	public String activityPoll() {
		
		if (isBlocking) {
			System.out.println("Polling activity messages " + isBlocking);		
			String msg = activityBlockingQueue.poll();
			System.out.println(msg + ", " + activityBlockingQueue.peek());
			System.out.println ("queue hash: " + System.identityHashCode(activityBlockingQueue));
			return msg;
		}
		else {
			if (activityQueue.isEmpty())
				return null;
			else
				return activityQueue.remove(0);
		}
	}
	
	public String errorPoll() {
		
		if (isBlocking) {
		
			return errorBlockingQueue.poll();
		}
		else {
			if (errorQueue.isEmpty())
				return null;
			else
				return errorQueue.remove(0);
		}
	}
	
	public IntegerProperty errorMessageFlag() { return errorFlag; };
	public IntegerProperty activityMessateFlag() { return activityFlag; };
		
}
