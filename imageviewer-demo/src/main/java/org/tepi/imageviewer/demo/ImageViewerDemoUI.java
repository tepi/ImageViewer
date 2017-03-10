package org.tepi.imageviewer.demo;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;

import org.tepi.imageviewer.ImageViewer;
import org.tepi.imageviewer.ImageViewer.ImageSelectionListener;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Title("ImageViewer Demo Application")
@Theme("demotheme")
public class ImageViewerDemoUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = ImageViewerDemoUI.class)
	public static class Servlet extends VaadinServlet {
	}

	private ImageViewer imageViewer;
	private VerticalLayout mainLayout;
	private TextField selectedImage = new TextField();

	@Override
	protected void init(VaadinRequest request) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		Label info = new Label(
				"<b>ImageViewer Demo Application</b>&nbsp;&nbsp;&nbsp;"
						+ "<i>Try the arrow keys, space/enter and home/end."
						+ " You can also click on the pictures or use the " + "mouse wheel.&nbsp;&nbsp;",
				ContentMode.HTML);

		imageViewer = new ImageViewer();
		imageViewer.setSizeFull();
		imageViewer.setImages(createImageList());
		imageViewer.setAnimationEnabled(false);
		imageViewer.setSideImageRelativeWidth(0.7f);

		imageViewer.addListener((ImageSelectionListener) e -> {
			selectedImage.setValue(e.getSelectedImageIndex() >= 0 ? String.valueOf(e.getSelectedImageIndex()) : "-");
		});
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSizeUndefined();
		hl.setMargin(false);
		hl.setSpacing(true);
		hl.addComponent(info);
		mainLayout.addComponent(hl);
		mainLayout.addComponent(imageViewer);
		mainLayout.setExpandRatio(imageViewer, 1);

		Layout ctrls = createControls();
		mainLayout.addComponent(ctrls);
		mainLayout.setComponentAlignment(ctrls, Alignment.BOTTOM_CENTER);

		Label images = new Label("Sample Photos: Bruno Monginoux / www.Landscape-Photo.net (cc-by-nc-nd)");
		images.setSizeUndefined();
		images.setStyleName("licence");
		mainLayout.addComponent(images);
		mainLayout.setComponentAlignment(images, Alignment.BOTTOM_RIGHT);

		setContent(mainLayout);
		imageViewer.setCenterImageIndex(0);
		imageViewer.focus();
	}

	private Layout createControls() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSizeUndefined();
		hl.setMargin(false);
		hl.setSpacing(true);

		CheckBox c = new CheckBox("HiLite");
		c.addValueChangeListener(e -> {
			imageViewer.setHiLiteEnabled(e.getValue());
			imageViewer.focus();
		});

		c.setValue(true);
		hl.addComponent(c);
		hl.setComponentAlignment(c, Alignment.BOTTOM_CENTER);

		c = new CheckBox("Animate");
		c.addValueChangeListener(e -> {
			imageViewer.setAnimationEnabled(e.getValue());
			imageViewer.focus();
		});
		c.setValue(true);
		hl.addComponent(c);
		hl.setComponentAlignment(c, Alignment.BOTTOM_CENTER);

		Slider s = new Slider("Animation duration (ms)");
		s.setMax(2000);
		s.setMin(200);
		s.setWidth("120px");
		s.addValueChangeListener(e -> {
			imageViewer.setAnimationDuration((int) Math.round(e.getValue()));
			imageViewer.focus();
		});
		s.setValue(350d);
		hl.addComponent(s);
		hl.setComponentAlignment(s, Alignment.BOTTOM_CENTER);

		s = new Slider("Center image width");
		s.setResolution(2);
		s.setMax(1);
		s.setMin(0.1);
		s.setWidth("120px");
		s.addValueChangeListener(e -> {
			imageViewer.setCenterImageRelativeWidth(e.getValue().floatValue());
			imageViewer.focus();
		});
		s.setValue(0.55);
		hl.addComponent(s);
		hl.setComponentAlignment(s, Alignment.BOTTOM_CENTER);

		s = new Slider("Side image count");
		s.setMax(5);
		s.setMin(1);
		s.setWidth("120px");

		s.addValueChangeListener(e -> {
			imageViewer.setSideImageCount((int) Math.round(e.getValue()));
			imageViewer.focus();
		});
		s.setValue(2d);
		hl.addComponent(s);
		hl.setComponentAlignment(s, Alignment.BOTTOM_CENTER);

		s = new Slider("Side image width");
		s.setResolution(2);
		s.setMax(0.8);
		s.setMin(0.5);
		s.setWidth("120px");

		s.addValueChangeListener(e -> {
			imageViewer.setSideImageRelativeWidth(e.getValue().floatValue());
			imageViewer.focus();
		});

		s.setValue(0.65);
		hl.addComponent(s);
		hl.setComponentAlignment(s, Alignment.BOTTOM_CENTER);

		s = new Slider("Horizontal padding");
		s.setMax(10);
		s.setMin(0);
		s.setWidth("120px");

		s.addValueChangeListener(e -> {
			imageViewer.setImageHorizontalPadding((int) Math.round(e.getValue()));
			imageViewer.focus();
		});
		s.setValue(1d);
		hl.addComponent(s);
		hl.setComponentAlignment(s, Alignment.BOTTOM_CENTER);

		s = new Slider("Vertical padding");
		s.setMax(10);
		s.setMin(0);
		s.setWidth("120px");
		s.addValueChangeListener(e -> {
			imageViewer.setImageVerticalPadding((int) Math.round(e.getValue()));
			imageViewer.focus();
		});
		s.setValue(5d);
		hl.addComponent(s);
		hl.setComponentAlignment(s, Alignment.BOTTOM_CENTER);

		selectedImage.setWidth("50px");
		hl.addComponent(selectedImage);
		hl.setComponentAlignment(selectedImage, Alignment.BOTTOM_CENTER);

		return hl;
	}

	/**
	 * Creates a list of Resources to be shown in the ImageViewer.
	 * 
	 * @return List of Resource instances
	 */
	private List<Resource> createImageList() {
		List<Resource> img = new ArrayList<Resource>();
		for (int i = 1; i < 10; i++) {
			img.add(new ThemeResource("images/" + i + ".jpg"));
		}
		return img;
	}
}
