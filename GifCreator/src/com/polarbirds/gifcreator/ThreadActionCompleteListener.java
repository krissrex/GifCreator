package com.polarbirds.gifcreator;

public interface ThreadActionCompleteListener {
	public static enum action {
		GIF_GENERATED, FILES_LOADED
	};
	
	public void actionComplete(action e);
}
