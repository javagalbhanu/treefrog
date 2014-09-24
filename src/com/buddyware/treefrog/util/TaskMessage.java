package com.buddyware.treefrog.util;

public final class TaskMessage {

	public enum TaskMessageType {
		TASK_ACTIVITY, TASK_ERROR
	};
	
	private final String message;
	private final TaskMessageType messageType;
	
	public TaskMessage (String message, TaskMessageType messageType) {
		this.message = message;
		this.messageType = messageType;
	};
	
	public String getMessage() {
		return message;
	}
	
	public TaskMessageType getMessageType() {
		return messageType;
	}
}
