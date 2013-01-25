package org.vaadin.tepi.imageviewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Resource;
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
@com.vaadin.ui.ClientWidget(org.vaadin.tepi.imageviewer.widgetset.client.ui.VImageViewer.class)
public class ImageViewer extends AbstractComponent implements Focusable {

    /** List of resources (images) set to display in this components */
    private List<? extends Resource> images = new ArrayList<Resource>();
    /** Amount of images on each side of the center image */
    private int sideImageCount = 2;
    /** Index of the currently centered image */
    private int centerImageIndex;
    /** Padding (in pixels) on the left and right side of each image */
    private int imageHorizontalPadding = 3;
    /** Padding (in pixels) on the top and bottom side of each image */
    private int imageVerticalPadding = 2;
    /** Ratio of total width the center image should consume */
    private float centerImageRelativeWidth = 0.4F;
    /**
     * Ratio by which each further side image will be reduced compared to the
     * previous one (or center image). Values of 0.5 to 0.8 are accepted.
     */
    private float sideImageRelativeWidth = 0.6F;
    /** Animations enabled */
    private boolean animationEnabled = true;
    /** Duration of a single animated action in milliseconds */
    private int animationDuration = 200;

    private int tabIndex;
    private boolean immediate = true;

    /** Are mouse over effects enabled */
    private boolean mouseOverEffects;
    /** List of registered image selection listeners */
    private LinkedList<ImageSelectionListener> listeners;

    /**
     * Default constructor of ImageViewer.
     * 
     * To set the images, use the setImages(List<? extends Resource> images)
     * method.
     */
    public ImageViewer() {
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
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        /* Add images as Resources to paint target */
        if (getImages() != null) {
            int amountOfImages = getImages().size();
            target.addAttribute("amountofimages", amountOfImages);
            target.startTag("resources");
            for (Resource r : getImages()) {
                target.startTag("resource");
                target.addAttribute("resource", r);
                target.endTag("resource");
            }
            target.endTag("resources");
        }
        target.addAttribute("immediate", immediate);
        target.addAttribute("mouseovereffects", mouseOverEffects);
        target.addAttribute("sideimages", sideImageCount);
        target.addAttribute("paddingx", imageHorizontalPadding);
        target.addAttribute("paddingy", imageVerticalPadding);
        target.addAttribute("centerImageRelativeWidth",
                centerImageRelativeWidth);
        target.addAttribute("sideImageRelativeWidth", sideImageRelativeWidth);
        target.addAttribute("animationEnabled", animationEnabled);
        target.addAttribute("animationDuration", animationDuration);
        target.addVariable(this, "centerimageindex", centerImageIndex);
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        if (variables.containsKey("centerimageindex")) {
            centerImageIndex = (Integer) variables.get("centerimageindex");
            if (listeners != null) {
                for (ImageSelectionListener l : listeners) {
                    l.imageSelected(new ImageSelectedEvent(this,
                            centerImageIndex));
                }
            }
        }
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
        this.images = images;
        if (isImmediate()) {
            requestRepaint();
        }
    }

    /**
     * Returns side image count.
     * 
     * @return Number of images displayed on each side of the center image
     */
    public int getSideImageCount() {
        return sideImageCount;
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
        this.sideImageCount = sideImageCount;
        if (isImmediate()) {
            requestRepaint();
        }
    }

    /**
     * Returns the index of the currently centered image.
     * 
     * @return Index of centered image; in relation to the List of Resources set
     *         to this ImageViewer
     */
    public int getCenterImageIndex() {
        return centerImageIndex;
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
        if (centerImageIndex > getImages().size() - 1) {
            throw new IllegalArgumentException(
                    "Given index must be present in the list of images.");
        }
        this.centerImageIndex = centerImageIndex;
        if (isImmediate()) {
            requestRepaint();
        }
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
        return imageHorizontalPadding;
    }

    /**
     * Sets a new image horizontal padding value.
     * 
     * @param imageHorizontalPadding
     *            new horizontal padding in pixels
     */
    public void setImageHorizontalPadding(int imageHorizontalPadding) {
        this.imageHorizontalPadding = imageHorizontalPadding;
        if (isImmediate()) {
            requestRepaint();
        }
    }

    /**
     * Returns current image vertical padding value.
     * 
     * @return vertical padding in pixels
     */
    public int getImageVerticalPadding() {
        return imageVerticalPadding;
    }

    /**
     * Sets a new image vertical padding value.
     * 
     * @param imageVerticalPadding
     *            new vertical padding in pixels
     */
    public void setImageVerticalPadding(int imageVerticalPadding) {
        this.imageVerticalPadding = imageVerticalPadding;
        if (isImmediate()) {
            requestRepaint();
        }
    }

    /**
     * Returns the relative width of the center image.
     * 
     * @return Width set for center image; this is relative to the width of this
     *         component. Should range from 0 to 1 (open).
     */
    public float getCenterImageRelativeWidth() {
        return centerImageRelativeWidth;
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
            throw new IllegalArgumentException(
                    "Relative widths must be between 0 and 1.");
        }
        this.centerImageRelativeWidth = centerImageRelativeWidth;
        if (isImmediate()) {
            requestRepaint();
        }
    }

    /**
     * Returns current side image relative width. This is the ratio by which the
     * size of each further side image will be reduced. Value should range from
     * 0.5 to 0.8.
     * 
     * @return current side image relative width.
     */
    public float getSideImageRelativeWidth() {
        return sideImageRelativeWidth;
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
            throw new IllegalArgumentException(
                    "Side image relative width must be between 0.5 and 0.8!");
        }
        this.sideImageRelativeWidth = sideImageRelativeWidth;
        if (isImmediate()) {
            requestRepaint();
        }
    }

    /**
     * Returns animation status.
     * 
     * @return true if animations are enabled
     */
    public boolean isAnimationEnabled() {
        return animationEnabled;
    }

    /**
     * Set animation status.
     * 
     * @param animationEnabled
     *            true to enable animations, false to disable
     */
    public void setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
        if (isImmediate()) {
            requestRepaint();
        }
    }

    /**
     * Returns the length set for one animation step; in milliseconds.
     * 
     * @return length of animation (ms)
     */
    public int getAnimationDuration() {
        return animationDuration;
    }

    /**
     * Sets a new length for one animation step; in milliseconds.
     * 
     * @param animationDuration
     *            new duration for animation (in ms)
     */
    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
        if (isImmediate()) {
            requestRepaint();
        }
    }

    /**
     * Returns true if mouse over effects are enabled.
     * 
     * @return true if enabled
     */
    public boolean isHiLiteEnabled() {
        return mouseOverEffects;
    }

    /**
     * Enables or disables mouse over effects.
     * 
     * @param mouseOverEffects
     *            true to enable effects
     */
    public void setHiLiteEnabled(boolean hiLiteEnabled) {
        mouseOverEffects = hiLiteEnabled;
        if (immediate) {
            requestRepaint();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#isImmediate()
     */
    @Override
    public boolean isImmediate() {
        return immediate;
    }

    /*
     * (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#setImmediate(boolean)
     */
    @Override
    public void setImmediate(boolean immediate) {
        this.immediate = immediate;
        if (immediate) {
            requestRepaint();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.vaadin.ui.AbstractComponent#focus()
     */
    @Override
    public void focus() {
        super.focus();
    }

    /*
     * (non-Javadoc)
     * @see com.vaadin.ui.Component.Focusable#getTabIndex()
     */
    public int getTabIndex() {
        return tabIndex;
    }

    /*
     * (non-Javadoc)
     * @see com.vaadin.ui.Component.Focusable#setTabIndex(int)
     */
    public void setTabIndex(int tabIndex) {
        if (tabIndex >= 0) {
            this.tabIndex = tabIndex;
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
         * @param e
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
        public ImageSelectedEvent(Object source, int selectedIndex) {
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
