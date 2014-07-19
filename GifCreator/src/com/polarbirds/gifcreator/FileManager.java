package com.polarbirds.gifcreator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

/**
 * 
 * @author Kristian
 * WIP.
 */
public class FileManager {
	
	private File path;
	private final List<BufferedImage> images;
	
	private ObservableList<File> allFiles;
	private ObservableList<File> selectedFiles;
	
	private final List<ThreadActionCompleteListener> listeners;

	
	public FileManager() {
		listeners = new ArrayList<ThreadActionCompleteListener>();
		images = new ArrayList<BufferedImage>();
	}
	
	public void setPath(File path) {
		this.path = path;
	}
	
	public File getPath() {
		return path; 
	}
	
	public void setAllFilesList(ObservableList<File> list) {
		allFiles = list;
	}
	
	public void setSelectedFilesList(ObservableList<File> list) {
		selectedFiles = list;
	}
	
	public void selectFile(int index) {
		if (-1 < index  && index < allFiles.size()) {
			File selectedFile = allFiles.get(index);
			if (selectedFile != null) {
				selectedFiles.add(selectedFile);
			}
		}
	}
	
	public void removeSelection(int index) {
		if (-1 < index && index < selectedFiles.size()) {
			selectedFiles.remove(index);
		}
	}
	
	/**
	 * @param index of the file to move.
	 * @return true if the file was moved, false otherwise.
	 */
	public boolean moveSelectionUp(int index) {
		//Valid range is [1, last file]
		if (0 < index && index < selectedFiles.size()) {
			File above = selectedFiles.get(index-1);
			selectedFiles.set(index-1, selectedFiles.get(index));
			selectedFiles.set(index, above);
			return true;
		}
		return false;
	}
	
	/**
	 * @param index of the file to move.
	 * @return true if the file was moved, false otherwise.
	 */
	public boolean moveSelectionDown(int index) {
		//Valid range is [0, last file - 1]
		if (0 <= index && index < selectedFiles.size()-1) {
			File below = selectedFiles.get(index+1);
			selectedFiles.set(index+1, selectedFiles.get(index));
			selectedFiles.set(index, below);
			return true;
		}
		return false;
	}
	
	public void loadPath() {
		File[] files = getPath().listFiles(
				new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						String[] extensions = ImageIO.getReaderFileSuffixes(); //Get an array containing "jpg", "bmp", "gif" etc.
						for (String ext :extensions) {
							if (name.endsWith(ext)) {
								//Add image extensions as accepted file types.
								return true;
							}
						}
						return false;
					}
				});
		
		if (allFiles != null) {
			allFiles.clear();
			for (File image : files) {
				allFiles.add(image);
			}
		}
	}
	
	public void loadImages() {
		//Make sure the loader does not rely on a mutable array.
		final File[] files = new File[selectedFiles.size()];
		for (int i = 0; i < selectedFiles.size(); i++) {
			files[i] = new File(selectedFiles.get(i).getPath());
		}
		final BufferedImage[] temp = new BufferedImage[files.length];
		
		//Create task
		Task<Void> loadTask = new Task<Void>() {
			
			@Override
			protected Void call() throws Exception {
				int i = 0;
				//TODO implement a way to cancel.
				for (File image : files) {
					try {
						// Possible optimization: load images into a map using path as key. avoids duplicates. Assign key to various indexes
						// in a string array. When adding images from the map to the ArrayList later, use the same BufferedImage references where
						// duplicates occur.
						temp[i] = ImageIO.read(image);
					} catch (Exception e) {}
					finally {
						i++;
					}
				}
				return null;
			}
		}; 
		
		//Set action to perform on success
		//inb4 "use lamba u filthy skrub"
		loadTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						images.clear();
						for (BufferedImage image : temp) {
							images.add(image);
						}
						
						for (ThreadActionCompleteListener listener : listeners) {
							listener.actionComplete(ThreadActionCompleteListener.action.FILES_LOADED);
						}
					}
				});
			}
		}); //Does this even work?
		
		//Run task
		Thread loadThread = new Thread(loadTask);
		loadThread.setDaemon(true); //Not sure what happens on false.
		loadThread.start();
	}
	
	/**
	 * Not implemented.
	 */
	public BufferedImage[] getImages() {
		BufferedImage[] out = new BufferedImage[images.size()];
		int i = 0;
		for (BufferedImage image : images) {
			out[i] = image;
			i++;
		}
		return out;
	}
	
	public void addListener(ThreadActionCompleteListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(ThreadActionCompleteListener listener) {
		listeners.remove(listener);
	}
}
