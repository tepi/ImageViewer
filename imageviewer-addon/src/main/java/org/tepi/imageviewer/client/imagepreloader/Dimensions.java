package org.tepi.imageviewer.client.imagepreloader;

public class Dimensions {
	private int width;
	private int height;

	public Dimensions(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public String toString() {
		return width + ", " + height;
	}
}