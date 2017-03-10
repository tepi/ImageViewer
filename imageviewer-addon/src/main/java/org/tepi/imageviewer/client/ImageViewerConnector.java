package org.tepi.imageviewer.client;

import org.tepi.imageviewer.ImageViewer;
import org.tepi.imageviewer.client.VImageViewer.ImageSelectionListener;

import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.client.ui.SimpleManagedLayout;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(ImageViewer.class)
public class ImageViewerConnector extends AbstractComponentConnector
		implements SimpleManagedLayout, ImageSelectionListener {

	private ImageViewerServerRpc rpc = RpcProxy.create(ImageViewerServerRpc.class, this);

	@Override
	public ImageViewerState getState() {
		return (ImageViewerState) super.getState();
	}

	@Override
	public void onStateChanged(StateChangeEvent stateChangeEvent) {
		super.onStateChanged(stateChangeEvent);

		/* If animation is currently running, ignore state change */
		/* The final queued animation will trigger a new state change anyway. */
		if (getWidget().animationRunning) {
			return;
		}
		getWidget().mouseOverEffects = getState().mouseOverEffects;
		getWidget().amountOfImages = getState().imageCount;
		getWidget().centerImageIndex = getState().centerImageIndex;
		/* Pass image URLs to widget */
		getWidget().urls = new String[getState().imageCount];
		for (int i = 0; i < getState().imageCount; i++) {
			getWidget().urls[i] = getResourceUrl("image-" + i);
		}
		/* Do not update side image count if the center image is maximized */
		if (getWidget().previousSideImages == 0) {
			getWidget().sideImages = getState().sideImageCount;
		}
		getWidget().centerImageWidth = getState().centerImageRelativeWidth;
		getWidget().sideImageReducePercentage = getState().sideImageRelativeWidth;
		getWidget().animationEnabled = getState().animationEnabled;
		getWidget().animationDuration = getState().animationDuration;
		getWidget().paddingX = getState().imageHorizontalPadding;
		getWidget().paddingY = getState().imageVerticalPadding;
		/* Render the images. */
		getWidget().fixSideImageCount();
		getWidget().renderImages();
	}

	@Override
	public VImageViewer getWidget() {
		VImageViewer widget = (VImageViewer) super.getWidget();
		widget.setImageSelectionListener(this);
		return widget;
	}

	@Override
	public void layout() {
		boolean heightUpdated = getWidget().updateHeight(getLayoutManager().getOuterHeight(getWidget().getElement()));
		boolean widthUpdated = getWidget().updateWidth(getLayoutManager().getOuterWidth(getWidget().getElement()));
		if (heightUpdated || widthUpdated) {
			getWidget().resizeImages();
		}
	}

	@Override
	public void centerImageSelected(int imageIndex) {
		rpc.centerImageSelected(imageIndex);
	}
}