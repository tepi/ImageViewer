package org.tepi.imageviewer.client;

import com.vaadin.shared.communication.ServerRpc;

public interface ImageViewerServerRpc extends ServerRpc {

	public void centerImageSelected(int newCenterImageIndex);

	public void moveFinished();
}
