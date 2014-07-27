package com.polarbirds.gifcreator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import com.polarbirds.gifcreator.ThreadActionEvent.Action;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
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
	private List<Task<Void>> loaders;
	
	public FileManager() {
		listeners = new ArrayList<ThreadActionCompleteListener>();
		images = new ArrayList<BufferedImage>();
		loaders = new ArrayList<Task<Void>>();
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
		
		///////////////////////////////////////////7
		
		
		
		//Create task
		Task<Void> loadTask = new Task<Void>() {
			
			@Override
			protected Void call() throws Exception {
				int i = 0;
				
				for (File image : files) {
					try {
						// Possible optimization: load images into a map using path as key. avoids duplicates. Assign key to various indexes
						// in a string array. When adding images from the map to the ArrayList later, use the same BufferedImage references where
						// duplicates occur.
						if (isCancelled()) {
							return null;
						}
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
				
				images.clear();
				for (BufferedImage image : temp) {
					images.add(image);
				}
				
				for (ThreadActionCompleteListener listener : listeners) {
					listener.actionComplete(new ThreadActionEvent(Action.FILES_LOADED));
				}
			}
		});
		
		loaders.add(loadTask);
		
		//Run task
		Thread loadThread = new Thread(null, loadTask, "ImageLoader");
		loadThread.setDaemon(true); //Not sure what happens on false.
		loadThread.start();
	}
	
	/**
	 * Cancels the loading thread.
	 */
	public void cancelLoading() {
		Iterator<Task<Void>> iter = loaders.iterator();
		while (iter.hasNext()) {
			Task<Void> loader = iter.next();
			if (loader == null || loader.getState() == State.CANCELLED || 
					loader.getState() == State.FAILED || loader.getState() == State.SUCCEEDED){
				iter.remove();
			} else {
				loader.cancel();
			}
		}
	}
	
	
	/**
	 * Returns the loaded images.
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
	
	public void saveGif(byte[] imageByteArray, File file) {
		ImageOutputStream a;
		
		
		try {
			System.out.println("Writing gif...");
			a = ImageIO.createImageOutputStream(new FileOutputStream(file));
			a.write(imageByteArray);
			System.out.println("Done writing gif. Stream pos: "+a.getStreamPosition());
			a.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}