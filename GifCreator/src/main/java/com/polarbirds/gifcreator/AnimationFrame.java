package com.polarbirds.gifcreator;

import java.awt.image.BufferedImage;

public class AnimationFrame {
	private BufferedImage image;
	private int delay;
	
	public AnimationFrame(BufferedImage image, int delay) {
		this.image = image;
		this.delay = delay;
	}
	
	/**
	 * Creates an image with delay 100 ms
	 * @param image
	 */
	public AnimationFrame(BufferedImage image) {
		this(image, 100);
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public void setImage(BufferedImage image) {
		this.image = image;		
	}
	
	public void setDelay(int delay) {
		this.delay = delay;
	}
	
	public int getDelay() {
		return delay;
	}
}
