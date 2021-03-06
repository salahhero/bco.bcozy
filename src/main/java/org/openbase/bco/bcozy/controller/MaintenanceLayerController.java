/**
 * ==================================================================
 *
 * This file is part of org.openbase.bco.bcozy.
 *
 * org.openbase.bco.bcozy is free software: you can redistribute it and modify
 * it under the terms of the GNU General Public License (Version 3)
 * as published by the Free Software Foundation.
 *
 * org.openbase.bco.bcozy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with org.openbase.bco.bcozy. If not, see <http://www.gnu.org/licenses/>.
 * ==================================================================
 */
package org.openbase.bco.bcozy.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javax.vecmath.Point3d;
import org.openbase.bco.bcozy.view.Constants;
import org.openbase.bco.bcozy.view.SimpleUnitSymbolsPane;
import org.openbase.bco.bcozy.view.location.LocationPane;
import org.openbase.bco.bcozy.view.pane.unit.TitledUnitPaneContainer;
import org.openbase.bco.dal.lib.layer.unit.UnitRemote;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rct.Transform;
import rst.domotic.registry.LocationRegistryDataType.LocationRegistryData;
import rst.domotic.registry.UnitRegistryDataType.UnitRegistryData;
import rst.domotic.state.EnablingStateType;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitTemplateType;
import rst.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;
import rst.geometry.PoseType;

/**
 * Controller for the pane for the maintenance layer of the room plan that includes buttons for the following units:
 * batteries, tamper detectors, temperature sensors, smoke detectors.
 * @author lili
 */
public class MaintenanceLayerController {

    /**
     * Application logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceLayerController.class);

    private final LocationPane locationPane;
    private final SimpleUnitSymbolsPane simpleUnitSymbolsPane;
    private final Map<String, TitledUnitPaneContainer> titledPaneMap;

    /**
     * Constructor
     *
     * @param unitPane
     * @param locationPane
     */
    public MaintenanceLayerController(final SimpleUnitSymbolsPane unitPane, final LocationPane locationPane) {
        this.locationPane = locationPane;
        this.simpleUnitSymbolsPane = unitPane;
        this.titledPaneMap = new HashMap<>();

        unitPane.scaleXProperty().bind(locationPane.scaleXProperty());
        unitPane.scaleYProperty().bind(locationPane.scaleYProperty());
        unitPane.translateXProperty().bind(locationPane.translateXProperty());
        unitPane.translateYProperty().bind(locationPane.translateYProperty());
    }

    /**
     * Establish the connection with the RemoteRegistry and fetch unit remotes.
     *
     * @throws org.openbase.jul.exception.CouldNotPerformException
     * @throws java.lang.InterruptedException
     */
    public void connectUnitRemote() throws CouldNotPerformException, InterruptedException {
        try {
            Registries.waitForData();
            Registries.getUnitRegistry().addDataObserver(new Observer<UnitRegistryData>() {
                @Override
                public void update(Observable<UnitRegistryData> source, UnitRegistryData data) throws InterruptedException {
                    Platform.runLater(() -> {
                        try {
                            fetchLocationUnitRemotes();
                            simpleUnitSymbolsPane.updateUnitsPane();
                        } catch (CouldNotPerformException ex) {
                            ExceptionPrinter.printHistory(ex, LOGGER);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    });
                }
            });
            Registries.getLocationRegistry().addDataObserver(new Observer<LocationRegistryData>() {
                @Override
                public void update(Observable<LocationRegistryData> source, LocationRegistryData data) throws Exception {
                    Platform.runLater(() -> {
                        try {
                            fetchLocationUnitRemotes();
                            simpleUnitSymbolsPane.updateUnitsPane();
                        } catch (CouldNotPerformException ex) {
                            ExceptionPrinter.printHistory(ex, LOGGER);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    });
                }

            });
            updateUnits();
        } catch (CouldNotPerformException ex) { //NOPMD
            throw new CouldNotPerformException("Could not fetch units from remote registry", ex);
        }
    }

    /**
     * Fetches all units for every location and saves them in the SimpleUnitSymbolsPane.
     *
     * @throws CouldNotPerformException
     * @throws InterruptedException
     */
    public void fetchLocationUnitRemotes() throws CouldNotPerformException, InterruptedException {
        
        simpleUnitSymbolsPane.clearUnits();
        final double halfButtonSize = (Constants.SMALL_ICON + (2 * Constants.INSETS)) / 2;

        final List<UnitConfig> locationUnitConfigList = Registries.getLocationRegistry().getLocationConfigs();

        for (final UnitConfig locationConfig : locationUnitConfigList) {

            // Only use locations with a valuable shape
            if (locationConfig.getPlacementConfig().getShape().getFloorCount() == 0) {
                continue;
            }

            for (final Map.Entry<UnitTemplateType.UnitTemplate.UnitType, List<UnitRemote>> nextEntry : Units.getUnit(locationConfig.getId(), false, Units.LOCATION).getUnitMap().entrySet()) {
                if (nextEntry.getValue().isEmpty()) {
                    continue;
                }

                for (UnitRemote<?> u : nextEntry.getValue()) {
                    if (nextEntry.getKey() == UnitType.BATTERY | nextEntry.getKey() == UnitType.TAMPER_DETECTOR | 
                        nextEntry.getKey() == UnitType.TEMPERATURE_SENSOR | nextEntry.getKey() == UnitType.SMOKE_DETECTOR) {

                        UnitConfig config = u.getConfig();
                        if (config.getEnablingState().getValue() != EnablingStateType.EnablingState.State.ENABLED) {
                            continue; 
                        }
                        if (!config.getPlacementConfig().hasPosition()) {
                            continue;
                        }

                        PoseType.Pose pose = config.getPlacementConfig().getPosition();
                        try {
                            final Future<Transform> transform = Registries.getLocationRegistry().getUnitTransformationFuture(config,
                                Registries.getLocationRegistry().getRootLocationConfig());
                            // transformation already in unit's coordinate space, therefore the zeros
                            final Point3d unitVertex = new Point3d(0.0, 0.0, 1.0);
                            transform.get(Constants.TRANSFORMATION_TIMEOUT / 10, TimeUnit.MILLISECONDS).
                                getTransform().transform(unitVertex);
                            Point2D coord = new Point2D(unitVertex.x * Constants.METER_TO_PIXEL, unitVertex.y * Constants.METER_TO_PIXEL);
                            // correction of position necessary because:
                            // "pose" is left bottom of unit bounding box (y correction) and the unit button's center 
                            // should be at the unit position (x correction) Attention: X and Y swapped in UnitButton 
                            simpleUnitSymbolsPane.addUnit(u, coord.add(- 0.5 * halfButtonSize, - halfButtonSize), locationConfig.getId());
                        } catch (CouldNotPerformException | ExecutionException | TimeoutException ex) {
                            // No exception throwing, because loop must continue it's work
                            ExceptionPrinter.printHistory(ex, LOGGER);

                        }
                    }
                }
            }
        }
    }

    /**
     * Fetches all unit remotes from registry and updates the unit pane,
     * so all unit buttons represent the correct configuration.
     */
    public void updateUnits() {
        Platform.runLater((() -> {
            try {
                fetchLocationUnitRemotes();
                simpleUnitSymbolsPane.updateUnitsPane();
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }));
    }
}
