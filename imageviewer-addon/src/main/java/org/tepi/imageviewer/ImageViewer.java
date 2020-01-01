package org.tepi.imageviewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

import org.tepi.imageviewer.client.Directions;
import org.tepi.imageviewer.client.ImageViewerServerRpc;
import org.tepi.imageviewer.client.ImageViewerState;

import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component.Focusable;

/**
 * ImageViewer is an add-on for Vaadin that allows a fancy image gallery style
 * display of a set of images.
 * 
 * ImageViewer supports e.g.
 * 
 * - Lazy-loading the images with the help of GWT Image Loader
 * 
 * - Animations
 * 
 * - Key actions for image browsing
 * 
 * - Mouse wheel actions for image browsing
 * 
 * - Mouse click actions for image browsing
 * 
 * - Maximizing and restoring the selected (center) image
 * 
 * - Black and normal Reindeer theme styles
 * 
 * - Adding listener for selected image index changes
 * 
 * - Immediate and non-immediate modes
 * 
 * - Providing the images as Vaadin Resources
 * 
 * @author Teppo Kurki
 */
@SuppressWarnings("serial")
public class ImageViewer extends AbstractComponent implements Focusable {

	/** List of resources (images) set to display in this components */
	public List<? extends Resource> images = new ArrayList<Resource>();

	/** List of registered image selection listeners */
	private LinkedList<ImageSelectionListener> listeners;

	/** Server RPC instance */
	private ImageViewerServerRpc rpc = new ImageViewerServerRpc() {

		@Override
		public void centerImageSelected(int newCenterImageIndex) {
			setCenterImageIndex(newCenterImageIndex);
			if (listeners != null) {
				for (ImageSelectionListener l : listeners) {
					l.imageSelected(new ImageSelectedEvent(this, newCenterImageIndex));
				}
			}
		}
		/**
		 * rpc call to reset the move direction
		 */
		@Override
		public void moveFinished() {
			ImageViewer.this.getState().direction = Directions.NODIRECTION;
		}
	};


	
	/**
	 * Default constructor of ImageViewer.
	 * 
	 * To set the images, use the {@link #setImages(List)}
	 * method.
	 */
	public ImageViewer() {
		registerRpc(rpc);
	}

	/**
	 * Convenience constructor that allows you to give the list of images at
	 * init.
	 * 
	 * @param images
	 *            List of images as resources
	 */
	public ImageViewer(List<? extends Resource> images) {
		setImages(images);
	}

	@Override
	protected ImageViewerState getState() {
		return (ImageViewerState) super.getState();
	}

	/**
	 * Trigger a movement of the current image to the left/right. Will trigger the animation, if the animation is enabled.
	 * 
	 * @param direction The direction the images will scroll.
	 */
	public void move(Directions direction) {
		this.getState().direction = direction;
	}

	/**
	 * Returns (an unmodifiable) copy of the list of images currently set to
	 * this ImageViewer.
	 * 
	 * @return List of Resource instances
	 */
	public List<? extends Resource> getImages() {
		return Collections.unmodifiableList(images);
	}

	/**
	 * Sets the given set of images to be displayed by this ImageViewer.
	 * 
	 * @param images
	 *            List of Resources
	 */
	public void setImages(List<? extends Resource> images) {
		if (images != null) {
			getState().imageCount = images.size();
			for (int i = 0; i < images.size(); i++) {
				setResource("image-" + i, images.get(i));
			}
		} else {
			getState().imageCount = 0;
		}

	}

	/**
	 * Returns side image count.
	 * 
	 * @return Number of images displayed on each side of the center image
	 */
	public int getSideImageCount() {
		return getState().sideImageCount;
	}

	/**
	 * Sets the side image count. Note that this value will be automatically
	 * adjusted if there are not enough images in the resource list.
	 * 
	 * @param sideImageCount
	 *            Number of images to be displayed on each side of the center
	 *            image. Value given must be over 1.
	 */
	public void setSideImageCount(int sideImageCount) {
		if (sideImageCount < 1) {
			throw new IllegalArgumentException("Minimum allowed value is 1.");
		}
		getState().sideImageCount = sideImageCount;
	}

	/**
	 * Returns the index of the currently centered image.
	 * 
	 * @return Index of centered image; in relation to the List of Resources set
	 *         to this ImageViewer
	 */
	public int getCenterImageIndex() {
		return getState().centerImageIndex;
	}

	/**
	 * Sets the center image index. The image with the given index will be
	 * centered.
	 * 
	 * @param centerImageIndex
	 *            Index of image to center; in relation to the List of Resources
	 *            set to this ImageViewer. Index must be present in the list.
	 */
	public void setCenterImageIndex(int centerImageIndex) {
		if (centerImageIndex > getState().imageCount - 1) {
			throw new IllegalArgumentException("Given index must be present in the list of images.");
		}
		getState().centerImageIndex = centerImageIndex;
		if (listeners != null) {
			for (ImageSelectionListener l : listeners) {
				l.imageSelected(new ImageSelectedEvent(this, centerImageIndex));
			}
		}
	}

	/**
	 * Returns current image horizontal padding value.
	 * 
	 * @return horizontal padding in pixels
	 */
	public int getImageHorizontalPadding() {
		return getState().imageHorizontalPadding;
	}

	/**
	 * Sets a new image horizontal padding value.
	 * 
	 * @param imageHorizontalPadding
	 *            new horizontal padding in pixels
	 */
	public void setImageHorizontalPadding(int imageHorizontalPadding) {
		getState().imageHorizontalPadding = imageHorizontalPadding;
	}

	/**
	 * Returns current image vertical padding value.
	 * 
	 * @return vertical padding in pixels
	 */
	public int getImageVerticalPadding() {
		return getState().imageVerticalPadding;
	}

	/**
	 * Sets a new image vertical padding value.
	 * 
	 * @param imageVerticalPadding
	 *            new vertical padding in pixels
	 */
	public void setImageVerticalPadding(int imageVerticalPadding) {
		getState().imageVerticalPadding = imageVerticalPadding;
	}

	/**
	 * Returns the relative width of the center image.
	 * 
	 * @return Width set for center image; this is relative to the width of this
	 *         component. Should range from 0 to 1 (open).
	 */
	public float getCenterImageRelativeWidth() {
		return getState().centerImageRelativeWidth;
	}

	/**
	 * Set a new relative width for the center image; this is relative to the
	 * width of this component.
	 * 
	 * @param centerImageRelativeWidth
	 *            new center image relative width. Allowed values range from 0
	 *            to 1 (open). Reasonable values lie between 0.3 and 0.7.
	 */
	public void setCenterImageRelativeWidth(float centerImageRelativeWidth) {
		if (centerImageRelativeWidth <= 0 || centerImageRelativeWidth >= 1) {
			throw new IllegalArgumentException("Relative widths must be between 0 and 1.");
		}
		getState().centerImageRelativeWidth = centerImageRelativeWidth;
	}

	/**
	 * Returns current side image relative width. This is the ratio by which the
	 * size of each further side image will be reduced. Value should range from
	 * 0.5 to 0.8.
	 * 
	 * @return current side image relative width.
	 */
	public float getSideImageRelativeWidth() {
		return getState().sideImageRelativeWidth;
	}

	/**
	 * Sets the ratio by which the size of each further side image will be
	 * reduced.
	 * 
	 * @param sideImageRelativeWidth
	 *            Ratio to multiply the side image size with. Accepted values
	 *            are between 0.5 and 0.8.
	 */
	public void setSideImageRelativeWidth(float sideImageRelativeWidth) {
		if (sideImageRelativeWidth < 0.5 || sideImageRelativeWidth > 0.8) {
			throw new IllegalArgumentException("Side image relative width must be between 0.5 and 0.8!");
		}
		getState().sideImageRelativeWidth = sideImageRelativeWidth;
	}

	/**
	 * Returns animation status.
	 * 
	 * @return true if animations are enabled
	 */
	public boolean isAnimationEnabled() {
		return getState().animationEnabled;
	}

	/**
	 * Set animation status.
	 * 
	 * @param animationEnabled
	 *            true to enable animations, false to disable
	 */
	public void setAnimationEnabled(boolean animationEnabled) {
		getState().animationEnabled = animationEnabled;
	}

	/**
	 * Returns the length set for one animation step; in milliseconds.
	 * 
	 * @return length of animation (ms)
	 */
	public int getAnimationDuration() {
		return getState().animationDuration;
	}

	/**
	 * Sets a new length for one animation step; in milliseconds.
	 * 
	 * @param animationDuration
	 *            new duration for animation (in ms)
	 */
	public void setAnimationDuration(int animationDuration) {
		getState().animationDuration = animationDuration;
	}

	/**
	 * Returns true if mouse over effects are enabled.
	 * 
	 * @return true if enabled
	 */
	public boolean isHiLiteEnabled() {
		return getState().mouseOverEffects;
	}

	/**
	 * Enables or disables mouse over effects.
	 * 
	 * @param hiLiteEnabled
	 *            true to enable effects
	 */
	public void setHiLiteEnabled(boolean hiLiteEnabled) {
		getState().mouseOverEffects = hiLiteEnabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.ui.AbstractComponent#focus()
	 */
	@Override
	public void focus() {
		super.focus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.ui.Component.Focusable#getTabIndex()
	 */
	public int getTabIndex() {
		return getState().tabIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vaadin.ui.Component.Focusable#setTabIndex(int)
	 */
	public void setTabIndex(int tabIndex) {
		if (tabIndex >= 0) {
			getState().tabIndex = tabIndex;
		}
	}

	/**
	 * Adds a new ImageSelectionListener to this ImageViewer. The listener will
	 * be notified when the selected (= centered) image is changed.
	 * 
	 * @param l
	 *            Listener to add
	 */
	public void addListener(ImageSelectionListener l) {
		if (listeners == null) {
			listeners = new LinkedList<ImageSelectionListener>();
		}
		listeners.add(l);
	}

	/**
	 * Removes the given ImageSelectionListener.
	 * 
	 * @param l
	 *            Listener to remove
	 */
	public void removeListener(ImageSelectionListener l) {
		if (listeners != null) {
			listeners.remove(l);
		}
	}

	/**
	 * ImageSelectionListener. Implement this interface to receive events from
	 * image selections.
	 */
	public interface ImageSelectionListener {
		/**
		 * This method is called when the selected/centered image is changed.
		 * 
		 * @param e image selected event
		 */
		public void imageSelected(ImageSelectedEvent e);
	}

	/**
	 * Image selection event. Fired to registered listeners when the centered
	 * image is changed.
	 */
	public class ImageSelectedEvent extends EventObject {
		private int selectedIndex = -1;

		/**
		 * Creates a new ImageSelectedEvent.
		 * 
		 * @param source
		 *            ImageViewer where the event happened
		 * @param selectedIndex
		 *            Index of the selected image
		 */
		private ImageSelectedEvent(Object source, int selectedIndex) {
			super(source);
			this.selectedIndex = selectedIndex;
		}

		/**
		 * Returns the index if the image that was selected to cause this
		 * effect.
		 * 
		 * @return Index of selected image
		 */
		public int getSelectedImageIndex() {
			return selectedIndex;
		}
	}
}
