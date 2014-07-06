package com.polarbirds.gifcreator;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/**
 * 
 * @author Kristian
 * WIP.
 */
public class FileManager implements Runnable {
	
	private File path;
	
	private ObservableList<File> allFiles;
	private ObservableList<File> selectedFiles;
	
	public FileManager() {}
	
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
	
	/**
	 * FIXME change the functionality. It should return File[] files from selectedFiles.
	 * @param path
	 * @param wind
	 */
	@Deprecated
	public String[] getFiles(String path, Window wind) {
		
		List<String> extensions = new ArrayList<String>();
		String[] validExtensions = ImageIO.getReaderFileSuffixes();
		
		for (String ext : validExtensions) {
			extensions.add("*."+ext);
		}
		
		FileChooser a = new FileChooser();
		a.setInitialDirectory(new File(path));
		a.setTitle("Choose images");
		a.getExtensionFilters().add(new ExtensionFilter("Image files", extensions));
		
		List<File> files = a.showOpenMultipleDialog(wind);
		
		
		String[] out = new String[files.size()];
		
		Iterator<File> it = files.iterator();
		
		int index = 0;
		while (it.hasNext() && index < out.length) {
			it.next();
			out[index] = it.next().getPath();
			index++;
		}
		
		return out;
	}
	
	/**
	 * WIP
	 * TODO I don't know yet. Loading images should be multi threaded.
	 * Might create a different, private class for it.
	 */
	@Override
	public void run() {
		
	}
}
