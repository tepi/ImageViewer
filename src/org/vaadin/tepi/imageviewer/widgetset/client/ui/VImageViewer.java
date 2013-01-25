package org.vaadin.tepi.imageviewer.widgetset.client.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 * VImageViewer is the client side widget of an add-on for Vaadin that allows a
 * fancy image gallery style display of a set of images.
 * 
 * @author Teppo Kurki
 */
public class VImageViewer extends FocusPanel implements Paintable,
        KeyDownHandler, ClickHandler, MouseWheelHandler {
    /** Style name */
    private static final String CLASSNAME = "v-imageviewer";

    /* ID and application connection references */
    private String paintableId;
    private ApplicationConnection client;

    /** Immediate mode */
    private boolean immediate;

    /** Widget root container */
    private final FlowPanel panelRoot;
    /** Container for the images */
    private final FlowPanel imageContainer;

    /* Size of widget */
    private int currentWidth;
    private int currentHeight;

    /** Center image width (percentage of total width) */
    private float centerImageWidth;

    /* Individual image margins */
    private int paddingX;
    private int paddingY;

    /** Index of centered image in relation to all images */
    private int centerImageIndex;

    /** Total amount of images */
    private int amountOfImages;

    /** Mouse over effects */
    private boolean mouseOverEffects;

    /** Amount of visible images on each side of the center image */
    private int sideImages;
    /** Amount of side images prior to enlarging the center image */
    private int previousSideImages;
    /** Each additional side image will be sized down by this factor */
    private float sideImageReducePercentage;

    /** List of URLs pointing to the images */
    private String[] urls = null;
    /**
     * Container for visible images NOTE: Contains also two images that are
     * hidden on both the left and right side of the actually visible images.
     */
    private VImage[] visibleImages;

    /** Are animations enabled */
    private boolean animationEnabled;
    /** Is an animation running */
    private boolean animationRunning;
    /** Duration of one animation in milliseconds */
    private int animationDuration;
    /** Queued animations, to be executed after the previous animation finishes */
    private List<Boolean> queuedAnimations = new ArrayList<Boolean>();

    /* Event handler registrations */
    private HandlerRegistration clickHandler, keyHandler, scrollHandler;

    public VImageViewer() {
        setStyleName(CLASSNAME);

        /* Create widget's root panel */
        panelRoot = new FlowPanel();
        panelRoot.setStyleName(CLASSNAME + "-flow");
        Style style = panelRoot.getElement().getStyle();
        style.setPosition(Position.RELATIVE);

        /* Create image container */
        imageContainer = new FlowPanel();
        style = imageContainer.getElement().getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setOverflow(Overflow.HIDDEN);

        /* Add widgets */
        panelRoot.add(imageContainer);
        add(panelRoot);

        /* Register handlers */
        keyHandler = addDomHandler(this, KeyDownEvent.getType());
        clickHandler = addDomHandler(this, ClickEvent.getType());
        scrollHandler = addDomHandler(this, MouseWheelEvent.getType());
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        if (client.updateComponent(this, uidl, true)) {
            return;
        }
        this.client = client;
        paintableId = uidl.getId();

        if (uidl.hasAttribute("immediate")) {
            immediate = uidl.getBooleanAttribute("immediate");
        }
        if (uidl.hasAttribute("mouseovereffects")) {
            mouseOverEffects = uidl.getBooleanAttribute("mouseovereffects");
        }
        if (uidl.hasAttribute("amountofimages")) {
            amountOfImages = uidl.getIntAttribute("amountofimages");
        }
        if (uidl.hasVariable("centerimageindex")) {
            centerImageIndex = uidl.getIntVariable("centerimageindex");
        }
        /* Parse URLs from resource UIDLs */
        if (uidl.getChildByTagName("resources") != null) {
            urls = new String[amountOfImages];
            UIDL resources = uidl.getChildByTagName("resources");
            Iterator<Object> i = resources.getChildIterator();
            int ix = 0;
            while (i.hasNext()) {
                UIDL resource = (UIDL) i.next();
                urls[ix] = client.translateVaadinUri(resource
                        .getStringAttribute("resource"));
                ix++;
            }
        }
        /* Do not update side image count if the center image is maximized */
        if (uidl.hasAttribute("sideimages") && previousSideImages == 0) {
            sideImages = uidl.getIntAttribute("sideimages");
        }
        if (uidl.hasAttribute("centerImageRelativeWidth")) {
            centerImageWidth = uidl
                    .getFloatAttribute("centerImageRelativeWidth");
        }
        if (uidl.hasAttribute("sideImageRelativeWidth")) {
            sideImageReducePercentage = uidl
                    .getFloatAttribute("sideImageRelativeWidth");
        }
        if (uidl.hasAttribute("animationEnabled")) {
            animationEnabled = uidl.getBooleanAttribute("animationEnabled");
        }
        if (uidl.hasAttribute("animationDuration")) {
            animationDuration = uidl.getIntAttribute("animationDuration");
        }
        if (uidl.hasAttribute("paddingx")) {
            paddingX = uidl.getIntAttribute("paddingx");
        }
        if (uidl.hasAttribute("paddingy")) {
            paddingY = uidl.getIntAttribute("paddingy");
        }

        /* Update widget dimensions */
        currentWidth = getElement().getScrollWidth();
        currentHeight = getElement().getScrollHeight();

        /*
         * Fix side image count to ensure that only a reasonable amount of
         * images is visible (= no duplicate images visible).
         */
        if (amountOfImages < 3) {
            sideImages = 0;
        } else if (2 * sideImages + 1 > amountOfImages) {
            sideImages = (amountOfImages - 1) / 2;
        }

        /* If images exist, render the images. Otherwise clear the panel. */
        if (amountOfImages > 0) {
            renderImages();
        } else {
            panelRoot.clear();
        }
    }

    @Override
    public void setWidth(String width) {
        int newWidth = 0;
        if (width == null || "".equals(width)) {
            newWidth = 200 + 100 * 2 * sideImages;
        } else {
            newWidth = Integer.parseInt(width.substring(0,
                    width.lastIndexOf("px")));
        }
        currentWidth = newWidth;
        Style style = getElement().getStyle();
        style.setWidth(newWidth, Unit.PX);
        style = imageContainer.getElement().getStyle();
        style.setWidth(newWidth, Unit.PX);
        /* Fix image sizes */
        resizeImages();
    }

    @Override
    public void setHeight(String height) {
        int newHeight = 0;
        if (height == null || "".equals(height)) {
            newHeight = 200;
        } else {
            newHeight = Integer.parseInt(height.substring(0,
                    height.lastIndexOf("px")));
        }
        currentHeight = newHeight;
        Style style = getElement().getStyle();
        style.setHeight(newHeight, Unit.PX);
        style = panelRoot.getElement().getStyle();
        style.setHeight(newHeight, Unit.PX);
        style = imageContainer.getElement().getStyle();
        style.setHeight(newHeight, Unit.PX);
        /* Fix image sizes */
        resizeImages();
    }

    /**
     * Handles key events
     */
    public void onKeyDown(KeyDownEvent event) {
        if (KeyCodes.KEY_HOME == event.getNativeKeyCode()) {
            centerImageIndex = 0;
            renderImages();
            client.updateVariable(paintableId, "centerimageindex",
                    centerImageIndex, immediate);
        } else if (KeyCodes.KEY_END == event.getNativeKeyCode()) {
            centerImageIndex = amountOfImages - 1;
            renderImages();
            client.updateVariable(paintableId, "centerimageindex",
                    centerImageIndex, immediate);
        } else if (event.isRightArrow() || event.isDownArrow()) {
            moveImages(false);
        } else if (event.isLeftArrow() || event.isUpArrow()) {
            moveImages(true);
        } else if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER
                || event.getNativeKeyCode() == 32) {
            imageClicked(visibleImages.length / 2);
        }
    }

    public void onClick(ClickEvent event) {
        setFocus(true);
    }

    public void onMouseWheel(MouseWheelEvent event) {
        if (animationRunning) {
            return;
        }
        event.preventDefault();
        moveImages(event.isNorth());
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        clickHandler.removeHandler();
        keyHandler.removeHandler();
        scrollHandler.removeHandler();
    }

    /**
     * Handle image click event. If the center image is clicked it will be
     * maximized and other images hidden. If some other image is clicked it will
     * be brought in the center.
     * 
     * @param index
     *            Index of the clicked image; relative to visibleImages array.
     */
    void imageClicked(int index) {
        int offset = visibleImages.length / 2 - index;
        if (offset > 0) {
            while (offset > 0) {
                moveImages(true);
                offset--;
            }
        } else if (offset < 0) {
            while (offset < 0) {
                moveImages(false);
                offset++;
            }
        } else {
            /* Only handle minimize/maximize when other animation are finished */
            if (!animationRunning) {
                if (sideImages == 0 && previousSideImages != 0) {
                    sideImages = previousSideImages;
                    previousSideImages = 0;
                    resizeCenterImage(false);
                } else {
                    previousSideImages = sideImages;
                    sideImages = 0;
                    resizeCenterImage(true);
                }
            }
        }
    }

    /**
     * Renders visible images to the image container
     */
    private void renderImages() {
        /* Empty the panel initially */
        imageContainer.clear();
        /* Determine amount of images to render */
        int imagesToRender = 1 + 2 * sideImages + 2;
        /* Calculate starting point */
        int startingPoint = centerImageIndex - sideImages;
        if (startingPoint < 0) {
            startingPoint += urls.length;
        }
        /* Add visible images */
        visibleImages = new VImage[imagesToRender];
        for (int i = 0; i < imagesToRender; i++) {
            VImage img = new VImage();
            if (i == 0 || i == imagesToRender - 1) {
                img.setVisible(false);
            }
            if (mouseOverEffects) {
                img.setMouseOverEffects(true);
                if (i == imagesToRender / 2) {
                    img.setCenter(true);
                }
                if (sideImages == 0 && previousSideImages != 0) {
                    img.setMaximized(true);
                }
            }
            img.setIndex(i);
            img.setOwner(this);
            img.setImageSource(urls[startingPoint]);
            img.setHorizontalMargin(paddingX);
            img.setVerticalMargin(paddingY);
            visibleImages[i] = img;
            imageContainer.add(img);
            startingPoint++;
            if (startingPoint > urls.length - 1) {
                startingPoint = 0;
            }
        }
        /* Fix image sizes */
        resizeImages();
    }

    /**
     * Calculates correct sizes for all visible images and sets them to the
     * VImage objects.
     */
    private void resizeImages() {
        if (visibleImages == null || visibleImages.length == 0) {
            return;
        }
        /* Get amount of images and center index */
        int images = visibleImages.length;
        int center = images / 2;
        int usedWidth = 0;
        /* Special case: only 1 image visible -> use all available space */
        if (sideImages == 0) {
            visibleImages[center].setCurrentWidth(currentWidth);
            visibleImages[center].setCurrentHeight(currentHeight);
            visibleImages[0].setCurrentWidth(0);
            visibleImages[0].setCurrentX(-2);
            visibleImages[2].setCurrentWidth(0);
            visibleImages[2].setCurrentX(currentWidth + 2);
            for (int i = 0; i < visibleImages.length; i++) {
                visibleImages[i].fixImageSizeAndPosition();
            }
            return;
        }
    
        /* Set center image size */
        visibleImages[center].setCurrentWidth(Math.round(centerImageWidth
                * currentWidth));
        usedWidth += Math.round((centerImageWidth * currentWidth));
        visibleImages[center].setCurrentHeight(currentHeight);
        /* Set center image position */
        visibleImages[center].setCurrentX(Math
                .round(((1 - centerImageWidth) / 2 * currentWidth)));
    
        /* Set side image sizes and positions */
        int nextWidth = 0;
        int leftPosition = Math
                .round(((1 - centerImageWidth) / 2 * currentWidth));
        int rightPosition = Math
                .round((float) ((0.5 + centerImageWidth / 2) * currentWidth) + 1);
        for (int i = 0; i < center; i++) {
            if (i < center - 2) {
                nextWidth = Math
                        .round(((currentWidth - usedWidth) / 2 * sideImageReducePercentage));
            } else if (i == (center - 2)) {
                nextWidth = (currentWidth - usedWidth) / 2;
            } else if (i == (center - 1)) {
                nextWidth = 0;
            }
            /* Side image on left */
            visibleImages[center - 1 - i].setCurrentWidth(nextWidth);
            visibleImages[center - 1 - i].setCurrentHeight(currentHeight);
            /* Side image on right */
            visibleImages[center + 1 + i].setCurrentWidth(nextWidth);
            visibleImages[center + 1 + i].setCurrentHeight(currentHeight);
            /* Update amount of width used */
            usedWidth += 2 * nextWidth;
            /* Set image positions */
            leftPosition -= nextWidth;
            visibleImages[center - 1 - i].setCurrentX(leftPosition);
            visibleImages[center + 1 + i].setCurrentX(rightPosition);
            rightPosition += nextWidth;
        }
        for (int i = 0; i < visibleImages.length; i++) {
            visibleImages[i].fixImageSizeAndPosition();
        }
    }

    /**
     * Moves the image set one step left or right. Animates the movement if
     * animations are enabled.
     * 
     * @param left
     *            true to move left; false to move right
     */
    private void moveImages(final boolean left) {
        if (animationEnabled && !animationRunning) {
            animationRunning = true;
            for (VImage img : visibleImages) {
                img.setVisible(true);
            }
            /* Set initial and target values to images */
            setInitialAndTargetValuesToImages(left);
            Animation animation = new Animation() {
                @Override
                protected void onUpdate(double progress) {
                    if (animationRunning) {
                        for (VImage img : visibleImages) {
                            updateAnimatedPositionAndWidth(img, progress);
                            img.getElement().getStyle().clearZIndex();
                        }
                        if (progress >= 1) {
                            finishAnimation(left);
                        }
                    }
                }
            };
            animation.run(animationDuration);
        } else if (animationEnabled && animationRunning) {
            queuedAnimations.add(left);
        } else {
            finishAnimation(left);
        }
    }

    /**
     * Sets start and end widths and positions to the visible images.
     * 
     * @param left
     *            true to move left; false to move right
     */
    private void setInitialAndTargetValuesToImages(boolean left) {
        for (int i = 0; i < visibleImages.length - 1; i++) {
            if (left) {
                visibleImages[i].initAnimation(
                        visibleImages[i + 1].getCurrentWidth(),
                        visibleImages[i + 1].getCurrentX());
            } else {
                visibleImages[i + 1].initAnimation(
                        visibleImages[i].getCurrentWidth(),
                        visibleImages[i].getCurrentX());
            }
        }
    }

    /**
     * Updates position and width of a single VImage object based on animation
     * progress.
     * 
     * @param img
     *            VImage to update
     * @param progress
     *            Progress of the animation
     */
    private void updateAnimatedPositionAndWidth(VImage img, double progress) {
        int widthDiff = img.getEndWidth() - img.getStartWidth();
        int newWidth = img.getStartWidth()
                + (int) Math.round(widthDiff * progress);

        int posDiff = img.getEndPosition() - img.getStartPosition();
        int newPos = img.getStartPosition()
                + (int) Math.round(posDiff * progress);

        img.setCurrentWidth(newWidth);
        img.setCurrentX(newPos);

        if (progress >= 1) {
            img.setCurrentX(img.getEndPosition());
            img.setCurrentWidth(img.getEndWidth());
        } else {
            img.fixImageSizeAndPosition();
        }
    }

    /**
     * Finishes animation; sets all field to the state they should be in after
     * the animation is completed. This is also used directly when the
     * animations are disabled.
     * 
     * @param left
     *            true to move left; false to move right
     */
    private void finishAnimation(boolean left) {
        animationRunning = false;
        centerImageIndex = left ? centerImageIndex - 1 : centerImageIndex + 1;
        if (left && centerImageIndex < 0) {
            centerImageIndex = amountOfImages - 1;
        }
        if (!left && centerImageIndex > amountOfImages - 1) {
            centerImageIndex = 0;
        }
        renderImages();
        if (!queuedAnimations.isEmpty()) {
            boolean nextAnimation = queuedAnimations.get(0);
            queuedAnimations.remove(0);
            moveImages(nextAnimation);
        }
        client.updateVariable(paintableId, "centerimageindex",
                centerImageIndex, immediate);
    }

    /**
     * Resizes the center image; either maximizes it or returns it to normal
     * size. Also fades out the side images on maximize. Runs the animation if
     * animations are enabled.
     * 
     * @param maximize
     *            true if you want to maximize the center image; false to return
     *            it back to normal size
     */
    private void resizeCenterImage(final boolean maximize) {
        if (animationEnabled && !animationRunning) {
            animationRunning = true;
            /* Set initial and target values to center image */
            final VImage centerImg = visibleImages[visibleImages.length / 2];
            if (maximize) {
                centerImg.initAnimation(currentWidth, 0);
                centerImg.getElement().getStyle().setZIndex(1);
            } else {
                centerImg.initAnimation(
                        Math.round(centerImageWidth * currentWidth),
                        (int) Math.floor((1 - centerImageWidth) / 2
                                * currentWidth));
                centerImg.getElement().getStyle().clearZIndex();
            }
            Animation animation = new Animation() {
                private boolean continueMaximize = true;
                private boolean startMinimize = false;

                @Override
                protected void onUpdate(double progress) {
                    if (animationRunning) {
                        for (VImage img : visibleImages) {
                            if (img != centerImg) {
                                updateOpacity(img, progress, maximize);
                                continue;
                            }
                            int newWidth = img.getStartWidth()
                                    + (int) Math.round((img.getEndWidth() - img
                                            .getStartWidth()) * progress);
                            if (maximize && continueMaximize) {
                                updateAnimatedPositionAndWidth(img, progress);
                                continueMaximize = !(newWidth > img
                                        .getImageAndMarginWidth());
                            }
                            startMinimize = newWidth < img
                                    .getImageAndMarginWidth();
                            if (!maximize && startMinimize) {
                                updateAnimatedPositionAndWidth(img, progress);
                            }
                        }
                        if (progress >= 1) {
                            animationRunning = false;
                            renderImages();
                        }
                    }
                }
            };
            animation.run(animationDuration);
        } else {
            renderImages();
        }
    }

    /**
     * Helper method for fading in or out a single image
     * 
     * @param img
     *            Image to apply the opacity to
     * @param progress
     *            State of the animation (0.0 - 1.0)
     * @param down
     *            true for fade-out, false for fade-in
     */
    private void updateOpacity(VImage img, double progress, boolean down) {
        Style style = img.getElement().getStyle();
        double newOpac = down ? progress >= 1 ? 0 : 1 - progress
                : progress >= 1 ? 1 : progress;
        style.setOpacity(newOpac);
        int newIntOpac = (int) (100 * newOpac);
        style.setProperty("filter", "alpha(opacity = " + newIntOpac + ")");
    }
}