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
package org.dc.bco.bcozy.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

/**
 * Created by hoestreich on 11/10/15.
 */
public class InfoFooter extends AnchorPane {

    private final Label mouseOverText;
    /**
     * Constructor for the InfoFooter.
     * @param height Height
     * @param width Width
     */
    public InfoFooter(final double height, final double width) {
        this.mouseOverText = new Label();
        this.mouseOverText.getStyleClass().add("small-label");
        this.mouseOverText.setAlignment(Pos.CENTER);
        this.getChildren().add(mouseOverText);
        this.setPrefHeight(height);
        this.setPrefWidth(width);
        this.getStyleClass().add("dropshadow-top-bg");
        this.getStyleClass().add("info-footer");
        this.setLeftAnchor(mouseOverText, Constants.INSETS);
        this.setRightAnchor(mouseOverText, Constants.INSETS);

    }

    /**
     * Getter for the actual mouseOverLabel.
     * @return the actual text which is set as a string
     */
    public Label getMouseOverText() {
        return mouseOverText;
    }

}