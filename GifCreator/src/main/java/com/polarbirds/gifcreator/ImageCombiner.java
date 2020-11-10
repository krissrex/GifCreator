package com.polarbirds.gifcreator;

import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.scene.image.Image;
import net.kroo.elliot.GifSequenceWriter;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ImageCombiner {
	
	private Image gif;
	private List<BufferedImage> images;
	private byte[] gifBytes;
	
	private int delay = 1;
	
	private List<OnImageCombineCompleteListener> listeners;
	private List<Task<Void>> generators;

	public ImageCombiner() {
		listeners = new ArrayList<>();
		images = new ArrayList<>();
		generators = new ArrayList<>();
	}
	
	public void addListener(OnImageCombineCompleteListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(OnImageCombineCompleteListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * 
	 * @return the generated gif. Returns null if none exists.
	 */
	public Image getGif() {
		return gif;
	}
	
	public byte[] getByteArray() {
		return gifBytes;
	}
	/**
	 * Sets the images to generate the gif from. The order is important.
	 * @param images
	 */
	public void setImages(BufferedImage[] images) {
		this.images.clear();
		for (BufferedImage image : images){
			this.images.add(image);
		}
	}
	
	
	/**
	 * Start a thread generating the gif.
	 */
	public void generate() {
		if (images.size() == 0) {
			gif = null;
			for (final OnImageCombineCompleteListener listener : listeners) {
				listener.onComplete(false);
			}
			return;
		}
		
		
		Task<Void> generateTask = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

			      BufferedImage firstImage = images.get(0);

			      ImageOutputStream output;
			      ByteArrayOutputStream byteArrayOs = new ByteArrayOutputStream();
			      
				try {
					output = ImageIO.createImageOutputStream(byteArrayOs);
			      
				      // create a gif sequence with the type of the first image, 1 second
				      // between frames, which loops continuously
				      GifSequenceWriter writer = new GifSequenceWriter(output,
				    		  firstImage.getType(), delay, true);
				      
				      // write out the images to the sequence...
				      for(BufferedImage image : images) {
				    	  if (isCancelled()) {
				    		  writer.close();
				    		  output.close();
				    		  gifBytes = null;
				    		  gif = null;
				    		  return null;
				    	  }
				        writer.writeToSequence(image);
				      }
				      
				      writer.close();
				      output.close();
				      gifBytes = byteArrayOs.toByteArray();
				      gif = new Image(new ByteArrayInputStream(gifBytes));
				      
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return null;
			}
			
		};
		
		generateTask.setOnSucceeded(workerStateEvent -> {
			for (final OnImageCombineCompleteListener listener : listeners) {
				listener.onComplete(true);
			}
		});
		
		generators.add(generateTask);
		
		//Run task
		Thread th = new Thread(null, generateTask, "GenerateThread");
		th.setDaemon(true);
		th.start();
	}

	public void cancelGeneration() {
		Iterator<Task<Void>> iter = generators.iterator();
		while (iter.hasNext()) {
			Task<Void> generator = iter.next();
			if (generator == null || generator.getState() == State.CANCELLED || 
					generator.getState() == State.FAILED || generator.getState() == State.SUCCEEDED){
				iter.remove();
			} else {
				generator.cancel();
			}
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

	@FunctionalInterface
	public interface OnImageCombineCompleteListener {
		void onComplete(boolean success);
	}
}
