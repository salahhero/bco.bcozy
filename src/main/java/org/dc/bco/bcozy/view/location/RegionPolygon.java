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

package org.dc.bco.bcozy.view.location;

import org.dc.bco.bcozy.view.Constants;

/**
 *
 */
public class RegionPolygon extends LocationPolygon {

    /**
     * The Constructor for a RegionPolygon.
     *
     * @param locationLabel The label of the location
     * @param locationId The id of the location
     * @param points The vertices of the location
     */
    public RegionPolygon(final String locationLabel, final String locationId, final double... points) {
        super(locationLabel, locationId, points);
    }

    @Override
    protected void setLocationStyle() {
        this.setFill(Constants.REGION_FILL);
        this.setStrokeWidth(0);
    }

    @Override
    protected void changeStyleOnSelection(final boolean selected) {

    }
}
