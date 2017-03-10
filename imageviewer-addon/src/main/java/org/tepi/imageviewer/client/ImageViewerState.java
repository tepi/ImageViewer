package org.tepi.imageviewer.client;

import com.vaadin.shared.ui.TabIndexState;

@SuppressWarnings("serial")
public class ImageViewerState extends TabIndexState {

	/** Amount of images on each side of the center image */
	public int sideImageCount = 2;
	/** Index of the currently centered image */
	public int centerImageIndex;
	/** Padding (in pixels) on the left and right side of each image */
	public int imageHorizontalPadding = 3;
	/** Padding (in pixels) on the top and bottom side of each image */
	public int imageVerticalPadding = 2;
	/** Ratio of total width the center image should consume */
	public float centerImageRelativeWidth = 0.4F;
	/**
	 * Ratio by which each further side image will be reduced compared to the
	 * previous one (or center image). Values of 0.5 to 0.8 are accepted.
	 */
	public float sideImageRelativeWidth = 0.6F;
	/** Animations enabled */
	public boolean animationEnabled = true;
	/** Duration of a single animated action in milliseconds */
	public int animationDuration = 200;
	/** Are mouse over effects enabled */
	public boolean mouseOverEffects;
	/** Amount of images added to the viewer */
	public int imageCount;
}
