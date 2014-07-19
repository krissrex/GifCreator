package com.polarbirds.gifcreator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import javafx.event.Event;
import javafx.scene.image.Image;

public class ImageCombiner {
	private List<ThreadActionCompleteListener> listeners;
	private Image gif;
	private List<BufferedImage> images;
	
	private int delay = 1;
	
	public ImageCombiner() {
		listeners = new ArrayList<ThreadActionCompleteListener>();
		images = new ArrayList<BufferedImage>();
	}
	
	public void addListener(ThreadActionCompleteListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(ThreadActionCompleteListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * 
	 * @return the generated gif. Returns null if none exists.
	 */
	public Image getGif() {
		return gif;
	}
	
	/**
	 * Sets the images to generate the gif from. The order is important.
	 * @param files
	 */
	public void setImages(BufferedImage[] images) {
		this.images.clear();
		for (BufferedImage image : images){
			this.images.add(image);
		}
	}
	
	
	/**
	 * WIP.
	 * TODO add functionality.
	 */
	public void generate() {
		/*if (images.size() == 0) {
			return;
		}*/
		
		//Multi thread this thing, and get the net.kroo.elliot.GifSequenceWriter to work.

		
		/*
		//Not runnable code. Excerpt from Controller debug code to make an Image
		// from a stream, usable in JavaFX.
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BufferedImage img = fm.getImages()[0];
		ImageIO.write(img, "gif", os); //"gif" might not be in imageWriters.
				
		Image a = new Image(new ByteArrayInputStream(os.toByteArray()));
		 */
		
		
		//On completion:
		for (ThreadActionCompleteListener listener : listeners) {
			listener.actionComplete(ThreadActionCompleteListener.action.GIF_GENERATED);
		}
	}
	
	public void setDelay(int delay) {
		if (delay > 0) {
			this.delay = delay;
		}
	}
	
	public int getDelay() {
		return delay;
	}
}
