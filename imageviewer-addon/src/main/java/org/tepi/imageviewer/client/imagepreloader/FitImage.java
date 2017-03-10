package org.tepi.imageviewer.client.imagepreloader;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Image;

/**
 * Sub-class of Image that allows maxWidth and maxHeight to be specified. When
 * the image loads it automatically resizes itself to maintain the correct
 * aspect ratio and fit within the maximum dimensions.
 * 
 * @author David Wolverton
 */
public class FitImage extends Image {
	private Integer maxWidth, maxHeight, fixedWidth, fixedHeight;

	private int height = 0;

	private int width = 0;

	private Double aspectRatio;

	private Dimensions dimensions;

	private void resize() {
		if (fixedWidth != null) {
			setWidth(fixedWidth);
			if (fixedHeight != null) {
				setHeight(fixedHeight);
			} else if (aspectRatio != null) {
				setHeight((int) Math.round(fixedWidth * aspectRatio));
			} else {
				setHeight(fixedWidth);
			}
		} else if (fixedHeight != null) {
			setHeight(fixedHeight);
			if (aspectRatio != null) {
				setWidth((int) Math.round(fixedHeight / aspectRatio));
			} else {
				setWidth(fixedHeight);
			}
		} else if (maxWidth != null) {
			if (maxHeight != null) {
				if (aspectRatio != null) {
					double maxAR = ((double) maxHeight) / ((double) maxWidth);
					if (aspectRatio > maxAR) {
						setHeight(maxHeight);
						setWidth((int) Math.round(maxHeight / aspectRatio));
					} else {
						setWidth(maxWidth);
						setHeight((int) Math.round(maxWidth * aspectRatio));
					}
				} else {
					setWidth(maxWidth);
					setHeight(maxHeight);
				}
			} else {
				setWidth(maxWidth);
				if (aspectRatio != null)
					setHeight((int) Math.round(maxWidth * aspectRatio));
				else
					setHeight(maxWidth);
			}
		} else if (maxHeight != null) {
			setHeight(maxHeight);
			if (aspectRatio != null)
				setWidth((int) Math.round(maxHeight / aspectRatio));
			else
				setWidth(maxHeight);
		} else {
			setWidth((Integer) null);
			setHeight((Integer) null);
		}
	}

	public FitImage() {
	}

	public FitImage(String url) {
		super();
		setUrl(url);
	}

	public FitImage(FitImageLoadHandler loadHandler) {
		super();
		addFitImageLoadHandler(loadHandler);
	}

	public FitImage(String url, FitImageLoadHandler loadHandler) {
		super();
		addFitImageLoadHandler(loadHandler);
		setUrl(url);
	}

	public FitImage(String url, int maxWidth, int maxHeight) {
		super();
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		setUrl(url);
		resize();
	}

	public FitImage(String url, int maxWidth, int maxHeight, FitImageLoadHandler loadHandler) {
		super();
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		addFitImageLoadHandler(loadHandler);
		setUrl(url);
		resize();
	}

	@Override
	public void setUrl(String url) {
		super.setUrl(url);
		ImagePreloader.load(url, new ImageLoadHandler() {
			public void imageLoaded(ImageLoadEvent event) {
				if (!event.isLoadFailed()) {
					dimensions = event.getDimensions();
					aspectRatio = ((double) dimensions.getHeight()) / ((double) dimensions.getWidth());
				}
				resize();
				fireEvent(new FitImageLoadEvent(event.isLoadFailed()));
			}
		});
	}

	public Integer getOriginalWidth() {
		return dimensions == null ? null : dimensions.getWidth();
	}

	public Integer getOriginalHeight() {
		return dimensions == null ? null : dimensions.getHeight();
	}

	/**
	 * <p>
	 * Handle FitImageLoadEvents. These events are fired whenever the image
	 * finishes loading completely or fails to load. The event occurs after the
	 * image has been resized to fit the original image aspect ratio.
	 * 
	 * <p>
	 * NOTE: Add this handler before setting the URL property of the FitImage.
	 * If set after, there is no guarantee that the handler will be fired for
	 * the event.
	 */
	public HandlerRegistration addFitImageLoadHandler(FitImageLoadHandler handler) {
		return addHandler(handler, FitImageLoadEvent.getType());
	}

	public Integer getMaxWidth() {
		return maxWidth;
	}

	/**
	 * The width of the image will never exceed this number of pixels.
	 */
	public void setMaxWidth(Integer maxWidth) {
		this.maxWidth = maxWidth;
		resize();
	}

	public Integer getMaxHeight() {
		return maxHeight;
	}

	/**
	 * The height of the image will never exceed this number of pixels.
	 */
	public void setMaxHeight(Integer maxHeight) {
		this.maxHeight = maxHeight;
		resize();
	}

	public void setMaxSize(Integer maxWidth, Integer maxHeight) {
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		resize();
	}

	public Integer getFixedWidth() {
		return fixedWidth;
	}

	/**
	 * The exact width (in pixels) for the image. This overrides the max
	 * dimension behavior, but preserves aspect ratio if fixedHeight is not also
	 * specified.
	 */
	public void setFixedWidth(Integer fixedWidth) {
		this.fixedWidth = fixedWidth;
		resize();
	}

	public Integer getFixedHeight() {
		return fixedHeight;
	}

	/**
	 * The exact height (in pixels) for the image. This overrides the max
	 * dimension behavior, but preserves aspect ratio if fixedWidth is not also
	 * specified.
	 */
	public void setFixedHeight(Integer fixedHeight) {
		this.fixedHeight = fixedHeight;
		resize();
	}

	public void setFixedSize(Integer fixedWidth, Integer fixedHeight) {
		this.fixedWidth = fixedWidth;
		this.fixedHeight = fixedHeight;
		resize();
	}

	private void setHeight(Integer px) {
		if (px == null) {
			setHeight("");
			height = 0;
		} else {
			super.setHeight(px + "px");
			height = px;
		}
	}

	public int getHeight() {
		return this.height;
	}

	private void setWidth(Integer px) {
		if (px == null) {
			setWidth("");
			width = 0;
		} else {
			super.setWidth(px + "px");
			width = px;
		}
	}

	public int getWidth() {
		return this.width;
	}
}
