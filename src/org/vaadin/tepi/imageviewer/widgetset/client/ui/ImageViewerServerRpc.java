package org.vaadin.tepi.imageviewer.widgetset.client.ui;

import com.vaadin.shared.communication.ServerRpc;

public interface ImageViewerServerRpc extends ServerRpc {

    public void centerImageSelected(int newCenterImageIndex);
}
