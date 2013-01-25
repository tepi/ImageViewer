package org.vaadin.tepi.imageviewer.demo;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.tepi.imageviewer.ImageViewer;
import org.vaadin.tepi.imageviewer.ImageViewer.ImageSelectedEvent;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Slider.ValueOutOfBoundsException;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class ImageViewerDemoApplication extends Application {

    private ImageViewer imageViewer;
    private VerticalLayout mainLayout;
    private TextField selectedImage = new TextField();

    @Override
    public void init() {
        Window mainWindow = new Window("ImageViewer Demo Application");
        mainWindow.setSizeFull();

        mainLayout = (VerticalLayout) mainWindow.getContent();
        mainLayout.addStyleName(Reindeer.LAYOUT_BLACK);
        mainLayout.setSizeFull();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        Label info = new Label(
                "<b>ImageViewer Demo Application</b>&nbsp;&nbsp;&nbsp;"
                        + "<i>Try the arrow keys, space/enter and home/end."
                        + " You can also click on the pictures or use the "
                        + "mouse wheel.&nbsp;&nbsp;", Label.CONTENT_XHTML);
        Button style = new Button("Click here to toggle style.");
        style.setStyleName(Reindeer.BUTTON_LINK);
        style.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                if (mainLayout.getStyleName().contains("black")) {
                    mainLayout.removeStyleName(Reindeer.LAYOUT_BLACK);
                } else {
                    mainLayout.addStyleName(Reindeer.LAYOUT_BLACK);
                }
            }
        });
        imageViewer = new ImageViewer();
        imageViewer.setSizeFull();
        imageViewer.setImages(createImageList());
        imageViewer.setAnimationEnabled(false);
        imageViewer.setSideImageRelativeWidth(0.7f);

        imageViewer.addListener(new ImageViewer.ImageSelectionListener() {
            public void imageSelected(ImageSelectedEvent e) {
                if (e.getSelectedImageIndex() >= 0) {
                    selectedImage.setValue(e.getSelectedImageIndex());
                } else {
                    selectedImage.setValue("-");
                }
            }
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeUndefined();
        hl.setMargin(false);
        hl.setSpacing(true);
        hl.addComponent(info);
        hl.addComponent(style);
        mainLayout.addComponent(hl);
        mainLayout.addComponent(imageViewer);
        mainLayout.setExpandRatio(imageViewer, 1);

        Layout ctrls = createControls();
        mainLayout.addComponent(ctrls);
        mainLayout.setComponentAlignment(ctrls, Alignment.BOTTOM_CENTER);

        Label images = new Label(
                "Sample Photos: Bruno Monginoux / www.Landscape-Photo.net (cc-by-nc-nd)");
        images.setSizeUndefined();
        images.setStyleName("licence");
        mainLayout.addComponent(images);
        mainLayout.setComponentAlignment(images, Alignment.BOTTOM_RIGHT);

        setMainWindow(mainWindow);
        imageViewer.setCenterImageIndex(0);
        imageViewer.focus();
        setTheme("imageviewertheme");
    }

    private Layout createControls() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeUndefined();
        hl.setMargin(false);
        hl.setSpacing(true);

        CheckBox c = new CheckBox("HiLite");
        c.setImmediate(true);
        c.addListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                boolean checked = (Boolean) event.getProperty().getValue();
                imageViewer.setHiLiteEnabled(checked);
                imageViewer.focus();
            }
        });
        c.setValue(true);
        hl.addComponent(c);
        hl.setComponentAlignment(c, Alignment.BOTTOM_CENTER);

        c = new CheckBox("Animate");
        c.setImmediate(true);
        c.addListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                boolean checked = (Boolean) event.getProperty().getValue();
                imageViewer.setAnimationEnabled(checked);
                imageViewer.focus();
            }
        });
        c.setValue(true);
        hl.addComponent(c);
        hl.setComponentAlignment(c, Alignment.BOTTOM_CENTER);

        Slider s = new Slider("Animation duration (ms)");
        s.setMin(200);
        s.setMax(2000);
        s.setImmediate(true);
        s.setWidth("120px");
        s.addListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                try {
                    int duration = (int) Math.round((Double) event
                            .getProperty().getValue());
                    imageViewer.setAnimationDuration(duration);
                    imageViewer.focus();
                } catch (Exception ignored) {
                }
            }
        });
        try {
            s.setValue(350);
        } catch (ValueOutOfBoundsException e) {
        }
        hl.addComponent(s);
        hl.setComponentAlignment(s, Alignment.BOTTOM_CENTER);

        s = new Slider("Center image width");
        s.setMin(0.1);
        s.setMax(1);
        s.setResolution(2);
        s.setImmediate(true);
        s.setWidth("120px");
        s.addListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                try {
                    double d = (Double) event.getProperty().getValue();
                    imageViewer.setCenterImageRelativeWidth((float) d);
                    imageViewer.focus();
                } catch (Exception ignored) {
                }
            }
        });
        try {
            s.setValue(0.55);
        } catch (ValueOutOfBoundsException e) {
        }
        hl.addComponent(s);
        hl.setComponentAlignment(s, Alignment.BOTTOM_CENTER);

        s = new Slider("Side image count");
        s.setMin(1);
        s.setMax(5);
        s.setImmediate(true);
        s.setWidth("120px");
        s.addListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                try {
                    int sideImageCount = (int) Math.round((Double) event
                            .getProperty().getValue());
                    imageViewer.setSideImageCount(sideImageCount);
                    imageViewer.focus();
                } catch (Exception ignored) {
                }
            }
        });
        try {
            s.setValue(2);
        } catch (ValueOutOfBoundsException e) {
        }
        hl.addComponent(s);
        hl.setComponentAlignment(s, Alignment.BOTTOM_CENTER);

        s = new Slider("Side image width");
        s.setMin(0.5);
        s.setMax(0.8);
        s.setResolution(2);
        s.setImmediate(true);
        s.setWidth("120px");
        s.addListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                try {
                    double d = (Double) event.getProperty().getValue();
                    imageViewer.setSideImageRelativeWidth((float) d);
                    imageViewer.focus();
                } catch (Exception ignored) {
                }
            }
        });
        try {
            s.setValue(0.65);
        } catch (ValueOutOfBoundsException e) {
        }
        hl.addComponent(s);
        hl.setComponentAlignment(s, Alignment.BOTTOM_CENTER);

        s = new Slider("Horizontal padding");
        s.setMin(0);
        s.setMax(10);
        s.setImmediate(true);
        s.setWidth("120px");
        s.addListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                try {
                    double d = (Double) event.getProperty().getValue();
                    imageViewer.setImageHorizontalPadding((int) Math.round(d));
                    imageViewer.focus();
                } catch (Exception ignored) {
                }
            }
        });
        try {
            s.setValue(1);
        } catch (ValueOutOfBoundsException e) {
        }
        hl.addComponent(s);
        hl.setComponentAlignment(s, Alignment.BOTTOM_CENTER);

        s = new Slider("Vertical padding");
        s.setMin(0);
        s.setMax(10);
        s.setImmediate(true);
        s.setWidth("120px");
        s.addListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                try {
                    double d = (Double) event.getProperty().getValue();
                    imageViewer.setImageVerticalPadding((int) Math.round(d));
                    imageViewer.focus();
                } catch (Exception ignored) {
                }
            }
        });
        try {
            s.setValue(5);
        } catch (ValueOutOfBoundsException e) {
        }
        hl.addComponent(s);
        hl.setComponentAlignment(s, Alignment.BOTTOM_CENTER);

        selectedImage.setWidth("50px");
        selectedImage.setImmediate(true);
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
