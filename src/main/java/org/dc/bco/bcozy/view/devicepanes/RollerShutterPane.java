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
package org.dc.bco.bcozy.view.devicepanes;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.dc.bco.bcozy.view.Constants;
import org.dc.bco.bcozy.view.SVGIcon;
import org.dc.jul.extension.rsb.com.AbstractIdentifiableRemote;
import org.dc.bco.dal.remote.unit.RollershutterRemote;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.printer.LogLevel;
import org.dc.jul.pattern.Observable;
import org.dc.jul.schedule.RecurrenceEventFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.homeautomation.state.ShutterStateType;
import rst.homeautomation.unit.RollershutterType;
import rst.homeautomation.state.ShutterStateType.ShutterState.State;

/**
 * Created by hoestreich on 11/19/15.
 */
public class RollerShutterPane extends UnitPane {

    private static final Logger LOGGER = LoggerFactory.getLogger(RollerShutterPane.class);

    private final RollershutterRemote rollershutterRemote;
    private final BorderPane headContent;
    private final HBox bodyContent;
    private final SVGIcon rollerShutterIconBackground;
    private final SVGIcon rollerShutterIconForeground;
    private final Rectangle clip;
    private final Text rollerShutterStatus;
    private final RecurrenceEventFilter recurrenceEventFilter;
    private final GridPane iconPane;
    private final Tooltip tooltip;
    private EventHandler<MouseEvent> sendingUp;
    private EventHandler<MouseEvent> sendingDown;
    private EventHandler<MouseEvent> sendingStop;
    private EventHandler<MouseEvent> sendingTotalOpening;
    private EventHandler<MouseEvent> sendingTotalClosing;

    /**
     * Constructor for a RollerShutterPane.
     * @param rollerShutterRemote AbstractIdentifiableRemote
     */
    public RollerShutterPane(final AbstractIdentifiableRemote rollerShutterRemote) {
        this.rollershutterRemote = (RollershutterRemote) rollerShutterRemote;

        rollerShutterIconBackground = new SVGIcon(MaterialDesignIcon.FORMAT_ALIGN_JUSTIFY, Constants.SMALL_ICON, false);
        rollerShutterIconForeground = new SVGIcon(MaterialDesignIcon.FORMAT_ALIGN_JUSTIFY, Constants.SMALL_ICON, true);
        headContent = new BorderPane();
        bodyContent = new HBox();
        rollerShutterStatus = new Text();
        iconPane = new GridPane();
        tooltip = new Tooltip();
        clip = new Rectangle();

        initUnitLabel();
        initTitle();
        initContent();
        createWidgetPane(headContent, bodyContent);
        initEffect();

        this.recurrenceEventFilter =  new RecurrenceEventFilter(100L) {
            @Override
            public void relay() throws Exception {
                mouseEvents();
            }
        };

        recurrenceEventFilter.trigger();


        this.rollershutterRemote.addObserver(this);
    }

    private void mouseEvents() {
        sendingUp = event -> new Thread(new Task() {
            @Override
            protected Object call() {
                try {
                    rollershutterRemote.setShutter(ShutterStateType.ShutterState.State.UP);
                } catch (CouldNotPerformException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                }
                return null;
            }
        }).start();

        sendingDown = event -> new Thread(new Task() {
            @Override
            protected Object call() {
                try {
                    rollershutterRemote.setShutter(ShutterStateType.ShutterState.State.DOWN);
                } catch (CouldNotPerformException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                }
                return null;
            }
        }).start();

        sendingStop = event -> new Thread(new Task() {
            @Override
            protected Object call() {
                try {
                    rollershutterRemote.setShutter(ShutterStateType.ShutterState.State.STOP);
                } catch (CouldNotPerformException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                }
                return null;
            }
        }).start();

        sendingTotalOpening = event -> new Thread(new Task() {
            @Override
            protected Object call() {
                try {
                    rollershutterRemote.setOpeningRatio(0.0);
                } catch (CouldNotPerformException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                }
                return null;
            }
        }).start();

        sendingTotalClosing = event -> new Thread(new Task() {
            @Override
            protected Object call() {
                try {
                    rollershutterRemote.setOpeningRatio(1.0);
                } catch (CouldNotPerformException e) {
                    ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
                }
                return null;
            }
        }).start();
    }

    private void initEffect() {
        State shutterState = State.UNKNOWN;
        double openingPercentage = 0.0;

        try {
            openingPercentage = rollershutterRemote.getOpeningRatio();
            shutterState = rollershutterRemote.getShutter().getValue();
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        setEffectOpeningRatio(openingPercentage, shutterState);
    }

    private void setEffectOpeningRatio(final double percentage, final State shutterState) {
        if (shutterState.equals(State.UNKNOWN)) {
            tooltip.setText(Constants.UNKNOWN);
            rollerShutterStatus.setText("??%");
            rollerShutterIconBackground.changeBackgroundIcon(MaterialDesignIcon.FORMAT_ALIGN_JUSTIFY);
            rollerShutterIconBackground.setForegroundIconColor(Color.YELLOW);
            rollerShutterIconForeground.changeForegroundIcon(MaterialDesignIcon.FORMAT_ALIGN_JUSTIFY);
            ((Rectangle) rollerShutterIconForeground.getClip()).setHeight(Constants.SMALL_ICON * percentage);
        } else  {
            if (shutterState.equals(State.DOWN)) {
                tooltip.setText(Constants.DOWN);
            } else if (shutterState.equals(State.UP)) {
                tooltip.setText(Constants.UP_STRING);
            } else {
                tooltip.setText(Constants.STOP);
            }
            rollerShutterIconBackground.changeBackgroundIcon(MaterialDesignIcon.FORMAT_ALIGN_JUSTIFY);
            rollerShutterIconBackground.setForegroundIconColor(Color.YELLOW);
            rollerShutterIconForeground.changeForegroundIcon(MaterialDesignIcon.FORMAT_ALIGN_JUSTIFY);

            ((Rectangle) rollerShutterIconForeground.getClip()).setHeight(Constants.SMALL_ICON * percentage);
            rollerShutterStatus.setText((int) (Constants.ONE_HUNDRED * percentage) + "%");
        }
        Tooltip.install(iconPane, tooltip);
    }

    @Override
    protected void initTitle() {
        iconPane.add(rollerShutterIconBackground, 0, 0);
        iconPane.add(rollerShutterIconForeground, 0, 0);
        iconPane.add(rollerShutterStatus, 1, 0);

        headContent.setLeft(iconPane);
        headContent.setCenter(getUnitLabel());
        headContent.setAlignment(getUnitLabel(), Pos.CENTER_LEFT);
        headContent.prefHeightProperty().set(Constants.SMALL_ICON + Constants.INSETS);

        clip.setWidth(Constants.SMALL_ICON);
        clip.setHeight(Constants.SMALL_ICON);

        rollerShutterIconForeground.setClip(clip);
    }

    @Override
    protected void initContent() { //NOPMD
        final Button buttonUp = new Button();
        final Button buttonDown = new Button();
        final Button buttonOpen = new Button();
        final Button buttonClose = new Button();
        final SVGIcon arrowUp;
        final SVGIcon arrowDown;
        final SVGIcon arrowDoubleUP;
        final SVGIcon arrowDoubleDown;

        arrowUp = new SVGIcon(MaterialDesignIcon.CHEVRON_UP, Constants.SMALL_ICON, true);
        arrowDown = new SVGIcon(MaterialDesignIcon.CHEVRON_DOWN, Constants.SMALL_ICON, true);
        arrowDoubleUP = new SVGIcon(MaterialDesignIcon.CHEVRON_DOUBLE_UP, Constants.SMALL_ICON, true);
        arrowDoubleDown = new SVGIcon(MaterialDesignIcon.CHEVRON_DOUBLE_DOWN, Constants.SMALL_ICON, true);

        buttonUp.setGraphic(arrowUp);
        buttonDown.setGraphic(arrowDown);
        buttonOpen.setGraphic(arrowDoubleUP);
        buttonClose.setGraphic(arrowDoubleDown);

        buttonOpen.setOnMouseClicked(sendingTotalOpening);
        buttonClose.setOnMouseClicked(sendingTotalClosing);
        buttonUp.setOnMousePressed(sendingUp);
        buttonUp.setOnMouseReleased(sendingStop);
        buttonDown.setOnMousePressed(sendingDown);
        buttonDown.setOnMouseReleased(sendingStop);

        bodyContent.getChildren().addAll(buttonOpen, buttonUp, buttonDown, buttonClose);
        bodyContent.setAlignment(Pos.CENTER);
        //CHECKSTYLE.OFF: MagicNumber
        bodyContent.prefHeightProperty().set(70 + Constants.INSETS);
        //CHECKSTYLE.ON: MagicNumber
    }

    @Override
    protected void initUnitLabel() {
        String unitLabel = Constants.UNKNOWN_ID;
        try {
            unitLabel = this.rollershutterRemote.getData().getLabel();
        } catch (CouldNotPerformException e) {
            ExceptionPrinter.printHistory(e, LOGGER, LogLevel.ERROR);
        }
        setUnitLabelString(unitLabel);
    }

    @Override
    public AbstractIdentifiableRemote getDALRemoteService() {
        return rollershutterRemote;
    }

    @Override
    void removeObserver() {
        this.rollershutterRemote.removeObserver(this);
    }

    @Override
    public void update(final Observable observable, final Object rollerShutter) throws java.lang.Exception {
        Platform.runLater(() -> {
            final double openingPercentage = ((RollershutterType.Rollershutter) rollerShutter).getOpeningRatio();
            final State shutterState = ((RollershutterType.Rollershutter) rollerShutter).getShutterState().getValue();
            setEffectOpeningRatio(openingPercentage, shutterState);
        });

    }
}