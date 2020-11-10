package com.polarbirds.gifcreator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Manages what image files are selected by the user.
 *
 * @author Kristian
 * WIP.
 */
public class FileManager {

	@Nullable
	private File path = null;
	private final List<BufferedImage> images;
	private final Map<File, AnimationFrame> imagesMap;
	private final ObservableList<File> allFiles;
	private final ObservableList<File> selectedFiles;
	
	private final List<OnLoadCompleteListener> listeners;
	private final List<Task<Void>> loaders;
	
	public FileManager() {
		listeners = new ArrayList<>();
		images = new ArrayList<>();
		imagesMap = new HashMap<>();
		loaders = new ArrayList<>();
		
		allFiles = FXCollections.observableArrayList();
		selectedFiles = FXCollections.observableArrayList();
	}
	
	public void setPath(File path) {
		this.path = path;
	}

	/**
	 * @return the currently active path
	 */
	@Nullable
	public File getPath() {
		return path; 
	}
	
	public ObservableList<File> getAllFilesList() {
		return allFiles;
	}
	
	public ObservableList<File> getSelectedFilesList() {
		return selectedFiles;
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
	
	public void clear() {
		selectedFiles.clear();
	}
	
	public void addAll() {
		selectedFiles.addAll(allFiles);
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
	
	public void sort(boolean ascending) {
		FXCollections.sort(selectedFiles, (fileA, fileB) -> {
			int order = ascending? 1 : -1;
			return order * fileA.getName().compareTo(fileB.getName());
		});
	}
	
	public void loadPath() {
		if (path == null) {
			return;
		}

		final String[] imageExtensions = ImageIO.getReaderFileSuffixes(); //Get an array containing "jpg", "bmp", "gif" etc.
		File[] files = path.listFiles(
				(dir, name) -> {
					for (final String ext : imageExtensions) {
						if (name.endsWith(ext)) {
							//Add image extensions as accepted file types.
							return true;
						}
					}
					return false;
				});
		
		if (allFiles != null) {
			allFiles.clear();
			if (files != null) {
				Collections.addAll(allFiles, files);
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
			protected Void call() {
				int i = 0;
				
				for (File image : files) {
					try {
						// Possible optimization: load images into a map using path as key. avoids duplicates. Assign key to various indexes
						// in a string array. When adding images from the map to the ArrayList later, use the same BufferedImage references where
						// duplicates occur.
						if (isCancelled()) {
							return null;
						}
						if (!imagesMap.containsKey(image)) {
							temp[i] = ImageIO.read(image);
							imagesMap.put(image, new AnimationFrame(temp[i]));
						}
					} catch (Exception e) {
						System.err.println("Failed to load image " + image);
					}
					finally {
						i++;
					}
				}
				
				return null;
			}
		}; 
		
		//Set action to perform on success
		loadTask.setOnSucceeded(workerStateEvent -> {
			images.clear();
			for (BufferedImage image : temp) {
				images.add(image);
			}

			//Don't store more than 15 spare images.
			int sizeLimit = 15+images.size();

			if (imagesMap.size() > sizeLimit) {
				Set<File> keys = imagesMap.keySet();
				keys.removeAll(selectedFiles);

				Iterator<File> iter = keys.iterator();
				while(iter.hasNext() && imagesMap.size() > sizeLimit) {
					imagesMap.remove(iter.next());
				}
			}

			for (OnLoadCompleteListener listener : listeners) {
				listener.onFileLoadComplete();
			}
		});
		
		loaders.add(loadTask);
		
		//Run task
		Thread loadThread = new Thread(null, loadTask, "ImageLoader");
		loadThread.setDaemon(true); // true -> program may exit if main tread exits while the loading thread still lives
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
	
	public AnimationFrame[] getFrames() {
		AnimationFrame[] frame = new AnimationFrame[selectedFiles.size()];
		int i = 0;
		for (File key : selectedFiles) {
			frame[i] = imagesMap.get(key);
			i++;
		}
		return frame;
	}
	
	public void addListener(OnLoadCompleteListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(OnLoadCompleteListener listener) {
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FunctionalInterface
	public interface OnLoadCompleteListener {
		void onFileLoadComplete();
	}
}