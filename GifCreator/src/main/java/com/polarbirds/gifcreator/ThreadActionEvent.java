package com.polarbirds.gifcreator;

public class ThreadActionEvent {
	
	public static enum Action {
		GIF_GENERATED, FILES_LOADED
	};
	
	private Action action;
	private boolean success;
	
	public ThreadActionEvent(Action action, boolean success) {
		this.action = action;
		this.success = success;
	}
	
	public ThreadActionEvent(Action action) {
		this.action = action;
		success = true;
	}
	
	public boolean succeeded() {
		return success;
	}
	
	public Action getAction() {
		return action;
	}
}
