package org.tepi.imageviewer.client.imagepreloader;

import com.google.gwt.event.shared.EventHandler;

public interface FitImageLoadHandler extends EventHandler {
	public void imageLoaded(FitImageLoadEvent event);
}