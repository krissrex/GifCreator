package com.polarbirds.gifcreator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import com.polarbirds.gifcreator.ThreadActionEvent.Action;

import net.kroo.elliot.GifSequenceWriter;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
		if (images.size() == 0) {
			for (ThreadActionCompleteListener listener : listeners) {
				listener.actionComplete(new ThreadActionEvent(Action.GIF_GENERATED, false));
			}
			return;
		}
		Task<Void> generateTask = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				boolean success = true;
				//Multi thread this thing, and get the net.kroo.elliot.GifSequenceWriter to work.


			      BufferedImage firstImage = images.get(0);

			      // create a new BufferedOutputStream with the last argument
			      ImageOutputStream output;
			      ByteArrayOutputStream byteArrayOs = new ByteArrayOutputStream();
			      
				try {
					output = ImageIO.createImageOutputStream(byteArrayOs);
				
			      
				      // create a gif sequence with the type of the first image, 1 second
				      // between frames, which loops continuously
				      GifSequenceWriter writer = new GifSequenceWriter(output,
				    		  firstImage.getType(), 100, true);
				      
				      // write out the images to the sequence...
				      for(BufferedImage image : images) {
				        writer.writeToSequence(image);
				      }
				      
				      writer.close();
				      output.close();
				      
				      gif = new Image(new ByteArrayInputStream(byteArrayOs.toByteArray()));
				      
				} catch (Exception e) {
					e.printStackTrace();
					success = false;
				} finally {
					//Notify about completion.
					final boolean succeeded = success;
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							for (ThreadActionCompleteListener listener : listeners) {
								listener.actionComplete(new ThreadActionEvent(Action.GIF_GENERATED, succeeded));
							}
						}
					});
				}
				return null;
			}
			
		};
		
		Thread th = new Thread(null, generateTask, "GenerateThread");
		th.setDaemon(true);
		th.start();
		
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
