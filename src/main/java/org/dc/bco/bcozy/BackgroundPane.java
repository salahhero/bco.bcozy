/**
 * ==================================================================
 *
 * This file is part of org.dc.bco.bcozy.
 *
 * org.dc.bco.bcozy is free software: you can redistribute it and modify
 * it under the terms of the GNU General Public License (Version 3)
 * as published by the Free Software Foundation.
 *
 * org.dc.bco.bcozy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with org.dc.bco.bcozy. If not, see <http://www.gnu.org/licenses/>.
 * ==================================================================
 */
package org.dc.bco.bcozy;

import javafx.scene.Group;
import javafx.scene.layout.*;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Created by hoestreich on 11/10/15.
 */
public class BackgroundPane extends StackPane {

    private static final double ZOOM_PANE_WIDTH = 2000;
    private static final double ZOOM_PANE_HEIGHT = 2000;

    private final Group locationViewContent;
    private RoomPolygon selectedRoom;

    /**
     * Constructor for the BackgroundPane.
     */
    public BackgroundPane() {

        final Rectangle emptyHugeRectangle = new Rectangle(-(ZOOM_PANE_WIDTH / 2),
                                                     -(ZOOM_PANE_HEIGHT / 2),
                                                     ZOOM_PANE_WIDTH,
                                                     ZOOM_PANE_HEIGHT);
        emptyHugeRectangle.setFill(Color.TRANSPARENT);

        //Dummy Room

        //CHECKSTYLE.OFF: MagicNumber
        selectedRoom = new RoomPolygon(0.0, 0.0, 0.0, 0.0);

        final RoomPolygon room0 = new RoomPolygon(50.0, 50.0,
                100.0, 50.0,
                100.0, 100.0,
                80.0, 100.0,
                80.0, 80.0,
                50.0, 80.0);

        final RoomPolygon room1 = new RoomPolygon(-10.0, -10.0,
                -10.0, 10.0,
                30.0, 30.0,
                30.0, -10.0);

        final RoomPolygon room2 = new RoomPolygon(50.0, -20.0,
                100.0, -20.0,
                100.0, 30.0,
                60.0, 30.0,
                60.0, 10.0,
                50.0, 10.0);

        final RoomPolygon room3 = new RoomPolygon(-30.0, 50.0,
                -10.0, 70.0,
                -10.0, 90.0,
                -30.0, 110.0,
                -50.0, 110.0,
                -70.0, 90.0,
                -70.0, 70.0,
                -50.0, 50.0);

        //CHECKSTYLE.ON: MagicNumber

        locationViewContent = new Group(emptyHugeRectangle, room0, room1, room2, room3);
        final ScrollPane scrollPane = createZoomPane(locationViewContent);

        final StackPane layout = new StackPane();
        layout.getChildren().setAll(scrollPane);

        final BackgroundImage backgroundImage = new BackgroundImage(
                new Image("blueprint.jpg"),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        layout.setBackground(new Background(backgroundImage));

        this.centerScrollPaneToPoint(scrollPane, this.getCenterOfGroup(locationViewContent, true));
        this.addMouseEventHandlerToRoom(scrollPane, room0, room1, room2, room3);
        this.getChildren().addAll(layout);
    }

    private ScrollPane createZoomPane(final Group group) {
        final double scaleDelta = 1.05;
        final StackPane zoomPane = new StackPane();

        zoomPane.getChildren().add(group);

        final ScrollPane scroller = new ScrollPane();
        final Group scrollContent = new Group(zoomPane);
        scroller.setContent(scrollContent);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.getStylesheets().add("transparent_scrollpane.css");

        //TODO: what iiiiiis is good for?
        scroller.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
            zoomPane.setMinSize(newValue.getWidth(), newValue.getHeight());
        });

        //CHECKSTYLE.OFF: MagicNumber
        scroller.setPrefViewportWidth(800);
        scroller.setPrefViewportHeight(600);
        //CHECKSTYLE.ON: MagicNumber

        zoomPane.setOnScroll(event -> {
            event.consume();

            if (event.getDeltaY() == 0) {
                return;
            }

            final double scaleFactor = (event.getDeltaY() > 0) ? scaleDelta : 1 / scaleDelta;

            // amount of scrolling in each direction in scrollContent coordinate
            // units
            final Point2D scrollOffset = figureScrollOffset(scrollContent, scroller);

            group.setScaleX(group.getScaleX() * scaleFactor);
            group.setScaleY(group.getScaleY() * scaleFactor);

            // move viewport so that old center remains in the center after the
            // scaling
            repositionScroller(scrollContent, scroller, scaleFactor, scrollOffset);

        });

        // Panning via drag....
        final ObjectProperty<Point2D> lastMouseCoordinates = new SimpleObjectProperty<>();
        scrollContent.setOnMousePressed(event -> lastMouseCoordinates.set(new Point2D(event.getX(), event.getY())));

        scrollContent.setOnMouseDragged(event -> {
            final double deltaX = event.getX() - lastMouseCoordinates.get().getX();
            final double extraWidth = scrollContent.getLayoutBounds().getWidth()
                                        - scroller.getViewportBounds().getWidth();
            final double deltaH = deltaX * (scroller.getHmax() - scroller.getHmin()) / extraWidth;
            final double desiredH = scroller.getHvalue() - deltaH;
            scroller.setHvalue(Math.max(0, Math.min(scroller.getHmax(), desiredH)));

            final  double deltaY = event.getY() - lastMouseCoordinates.get().getY();
            final double extraHeight = scrollContent.getLayoutBounds().getHeight()
                                        - scroller.getViewportBounds().getHeight();
            final double deltaV = deltaY * (scroller.getHmax() - scroller.getHmin()) / extraHeight;
            final double desiredV = scroller.getVvalue() - deltaV;
            scroller.setVvalue(Math.max(0, Math.min(scroller.getVmax(), desiredV)));
        });

        return scroller;
    }

    private void centerScrollPaneToPoint(final ScrollPane scroller, final Point2D center) {
        final double realZoomPaneWidth = ZOOM_PANE_WIDTH - scroller.getWidth() / locationViewContent.getScaleX();
        final double realZoomPaneHeight = ZOOM_PANE_HEIGHT - scroller.getHeight() / locationViewContent.getScaleY();
        scroller.setHvalue((center.getX() + (realZoomPaneWidth / 2.0)) / realZoomPaneWidth);
        scroller.setVvalue((center.getY() + (realZoomPaneHeight / 2.0)) / realZoomPaneHeight);
    }

    private void centerScrollPaneToPointAnimated(final ScrollPane scroller, final Point2D center) {
        final double realZoomPaneWidth = ZOOM_PANE_WIDTH - scroller.getWidth() / locationViewContent.getScaleX();
        final double realZoomPaneHeight = ZOOM_PANE_HEIGHT - scroller.getHeight() / locationViewContent.getScaleY();

        final Timeline timeline = new Timeline();
        timeline.setCycleCount(1);

        final KeyValue keyValueX = new KeyValue(scroller.hvalueProperty(),
                                        (center.getX() + (realZoomPaneWidth / 2.0)) / realZoomPaneWidth,
                                        Interpolator.EASE_BOTH);
        final KeyValue keyValueY = new KeyValue(scroller.vvalueProperty(),
                                        (center.getY() + (realZoomPaneHeight / 2.0)) / realZoomPaneHeight,
                                        Interpolator.EASE_BOTH);
        final KeyFrame keyFrame = new KeyFrame(Duration.millis(500), keyValueX, keyValueY);

        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    private void addMouseEventHandlerToRoom(final ScrollPane scrollPane, final RoomPolygon room) {
        room.setOnMouseClicked(event -> {
            event.consume();

            centerScrollPaneToPointAnimated(scrollPane, new Point2D(room.getCenterX(), room.getCenterY()));
            if (!room.isSelected()) {
                selectedRoom.toggleSelected();
                room.toggleSelected();
                selectedRoom = room;
            }
        });
    }

    private void addMouseEventHandlerToRoom(final ScrollPane scrollPane, final RoomPolygon... room) {
        for (final RoomPolygon currentRoom : room) {
            this.addMouseEventHandlerToRoom(scrollPane, currentRoom);
        }
    }

    private Point2D getCenterOfGroup(final Group group, final boolean skipFirst) {
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;

        int start;
        if (skipFirst) {
            start = 1;
        } else {
            start = 0;
        }

        for (int i = start; i < group.getChildren().size(); i++) {
            if (group.getChildren().get(i).getLayoutBounds().getMaxX() > maxX) {
                maxX = group.getChildren().get(i).getLayoutBounds().getMaxX();
            }
            if (group.getChildren().get(i).getLayoutBounds().getMaxY() > maxY) {
                maxY = group.getChildren().get(i).getLayoutBounds().getMaxY();
            }
            if (group.getChildren().get(i).getLayoutBounds().getMinX() < minX) {
                minX = group.getChildren().get(i).getLayoutBounds().getMinX();
            }
            if (group.getChildren().get(i).getLayoutBounds().getMinY() < minY) {
                minY = group.getChildren().get(i).getLayoutBounds().getMinY();
            }
        }

        return new Point2D((minX + maxX) / 2, (minY + maxY) / 2);
    }

    private Point2D figureScrollOffset(final Node scrollContent, final ScrollPane scroller) {
        final double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
        final double hScrollProportion = (scroller.getHvalue() - scroller.getHmin())
                                    / (scroller.getHmax() - scroller.getHmin());
        final double scrollXOffset = hScrollProportion * Math.max(0, extraWidth);
        final double extraHeight = scrollContent.getLayoutBounds().getHeight()
                                    - scroller.getViewportBounds().getHeight();
        final double vScrollProportion = (scroller.getVvalue() - scroller.getVmin())
                                    / (scroller.getVmax() - scroller.getVmin());
        final double scrollYOffset = vScrollProportion * Math.max(0, extraHeight);
        return new Point2D(scrollXOffset, scrollYOffset);
    }

    private void repositionScroller(final Node scrollContent, final ScrollPane scroller,
                                    final double scaleFactor, final Point2D scrollOffset) {
        final double scrollXOffset = scrollOffset.getX();
        final double scrollYOffset = scrollOffset.getY();
        final double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
        if (extraWidth > 0) {
            final double halfWidth = scroller.getViewportBounds().getWidth() / 2;
            final double newScrollXOffset = (scaleFactor - 1) *  halfWidth + scaleFactor * scrollXOffset;
            scroller.setHvalue(scroller.getHmin()
                    + newScrollXOffset * (scroller.getHmax() - scroller.getHmin()) / extraWidth);
        } else {
            scroller.setHvalue(scroller.getHmin());
        }
        final double extraHeight = scrollContent.getLayoutBounds().getHeight()
                                    - scroller.getViewportBounds().getHeight();
        if (extraHeight > 0) {
            final double halfHeight = scroller.getViewportBounds().getHeight() / 2;
            final double newScrollYOffset = (scaleFactor - 1) * halfHeight + scaleFactor * scrollYOffset;
            scroller.setVvalue(scroller.getVmin()
                    + newScrollYOffset * (scroller.getVmax() - scroller.getVmin()) / extraHeight);
        } else {
            scroller.setHvalue(scroller.getHmin());
        }
    }
}