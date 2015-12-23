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
package org.dc.bco.bcozy.view.mainmenupanes;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.dc.bco.bcozy.view.Constants;
import org.dc.bco.bcozy.view.SVGIcon;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by hoestreich on 11/24/15.
 */
public class UserPane extends VBox {

    private final PaneElement loginPane;
    private final PaneElement startLoginPane;
    private final PaneElement logoutPane;
    private final Button startLoginBtn;
    private final Button loginBtn;
    private final Button backBtn;
    private final Button logoutBtn;
    private final TextField nameTxt;
    private final PasswordField passwordField;
    private final Label inputWrongLbl;
    private final Label loggedInUserLbl;
    private final VBox loginLayout;
    private final Label nameLbl;
    private final Label pwLbl;
    /**
     * Enum to control the display state.
     */
    public enum State { LOGINACTIVE, LOGIN, LOGOUT }

    /**
     * Constructor for the UserPane.
     */
    public UserPane() {

        final ResourceBundle languageBundle = ResourceBundle
                .getBundle(Constants.LANGUAGE_RESOURCE_BUNDLE, Locale.getDefault());

        // Case: Before login
        startLoginBtn = new Button("", new SVGIcon(MaterialDesignIcon.LOGIN, Constants.SMALL_ICON));
        startLoginPane = new PaneElement(startLoginBtn);

        // Case: Login active
        nameLbl = new Label(languageBundle.getString("username"));
        nameLbl.setAlignment(Pos.BOTTOM_LEFT);
        nameTxt = new TextField();
        pwLbl = new Label(languageBundle.getString("password"));
        passwordField = new PasswordField();
        inputWrongLbl = new Label(languageBundle.getString("inputWrong"));
        inputWrongLbl.setAlignment(Pos.TOP_LEFT);
        //inputWrongLbl.setVisible(false);
        loginBtn = new Button(languageBundle.getString("login"));
        final HBox rightAlignLoginButton = new HBox(loginBtn);
        rightAlignLoginButton.setAlignment(Pos.CENTER_RIGHT);

        backBtn = new Button("", new SVGIcon(FontAwesomeIcon.ARROW_LEFT, Constants.EXTRA_SMALL_ICON));

        loginLayout = new VBox(Constants.INSETS);
        final BorderPane loginFirstLineLayout = new BorderPane();
        loginFirstLineLayout.setLeft(nameLbl);
        loginFirstLineLayout.setRight(backBtn);
        loginLayout.getStyleClass().clear();
        loginLayout.setAlignment(Pos.BOTTOM_LEFT);
        loginLayout.getChildren().addAll(loginFirstLineLayout, nameTxt, pwLbl, passwordField,
                rightAlignLoginButton);
        loginPane = new PaneElement(loginLayout);
        loginPane.setMaxWidth(Constants.MAX_MENU_WIDTH);

        //Case: User logged in
        final VBox logoutLayout = new VBox(Constants.INSETS);
        final SVGIcon loggedInUserIcon = new SVGIcon(MaterialDesignIcon.ACCOUNT_CIRCLE, Constants.SMALL_ICON);
        loggedInUserLbl = new Label();
        logoutBtn = new Button(languageBundle.getString("logout"));
        final HBox rightAlignLogoutButton = new HBox(logoutBtn);
        rightAlignLogoutButton.setAlignment(Pos.CENTER_RIGHT);

        logoutLayout.getStyleClass().clear();
        logoutLayout.setAlignment(Pos.TOP_CENTER);
        logoutLayout.getChildren().addAll(loggedInUserIcon, loggedInUserLbl, rightAlignLogoutButton);
        logoutPane = new PaneElement(logoutLayout);
        logoutPane.setMaxWidth(Constants.MAX_MENU_WIDTH);

        this.setFillWidth(true);
        this.setSpacing(Constants.INSETS);

        //Setting styles
        //CHECKSTYLE.OFF: MultipleStringLiterals
        nameLbl.getStyleClass().clear();
        nameLbl.getStyleClass().add("small-label");
        inputWrongLbl.getStyleClass().clear();
        inputWrongLbl.getStyleClass().add("wrong-input-indicator");
        pwLbl.getStyleClass().clear();
        pwLbl.getStyleClass().add("small-label");
        loginBtn.getStyleClass().clear();
        loginBtn.getStyleClass().add("transparent-button");
        logoutBtn.getStyleClass().clear();
        logoutBtn.getStyleClass().add("transparent-button");
        //CHECKSTYLE.ON: MultipleStringLiterals

        this.getChildren().addAll(startLoginPane);
    }

    /**
     * Getter for the startLogin button which starts the user login.
     * @return instance of the button
     */
    public Button getStartLoginBtn() {
        return startLoginBtn;
    }

    /**
     * Getter for the login button which initiates the user login.
     * @return instance of the button
     */
    public Button getLoginBtn() {
        return loginBtn;
    }

    /**
     * Getter for the back button to abort a login.
     * @return instance of the button
     */
    public Button getBackBtn() {
        return backBtn;
    }

    /**
     * Getter for the name textfield.
     * @return instance of the textfield
     */
    public TextField getNameTxt() {
        return nameTxt;
    }

    /**
     * Getter for the passwordfield.
     * @return instance of the passwordfield
     */
    public PasswordField getPasswordField() {
        return passwordField;
    }

    /**
     * Getter for the logoutBtn.
     * @return instance of the logoutBtn
     */
    public Button getLogoutBtn() {
        return logoutBtn;
    }

    /**
     * Getter for the inputWrongLabel.
     * @return instance of the inputWrongLbl
     */
    public Label getInputWrongLbl() {
        return inputWrongLbl;
    }

    /**
     * Getter for the loggedInUserLbl.
     * @return instance of the loggedInUserLbl
     */
    public Label getLoggedInUserLbl() {
        return loggedInUserLbl;
    }

    /**
     * Getter for the pwLbl.
     * @return instance of the pwLbl
     */
    public Label getPwLbl() {
        return pwLbl;
    }

    /**
     * Getter for the nameLbl.
     * @return instance of the nameLbl
     */
    public Label getNameLbl() {
        return nameLbl;
    }

    /**
     * Change CSS Style to indicate that at least one of the informations
     * password or the name were wrong.
     */
    public void indicateUserOrPasswordWrong() {
        if (!loginLayout.getChildren().contains(inputWrongLbl)) {
            passwordField.getStyleClass().add("password-field-wrong");
            nameTxt.getStyleClass().add("text-field-wrong");
            loginLayout.getChildren().add(loginLayout.getChildren().size() - 1, inputWrongLbl);
        }
    }

    /**
     * Reset CSS Style if name or password are corrected.
     */
    public void resetUserOrPasswordWrong() {
        passwordField.getStyleClass().clear();
        nameTxt.getStyleClass().clear();
        passwordField.getStyleClass().add("password-field");
        nameTxt.getStyleClass().add("text-field");
        if (loginLayout.getChildren().contains(inputWrongLbl)) {
            loginLayout.getChildren().remove(inputWrongLbl);
        }
    }
    /**
     * GUI Method to switch the displayed panes.
     * @param state A state from the defined Enum
     */
    public void setState(final State state) {
        switch (state) {

            case LOGINACTIVE:
                this.getChildren().clear();
                this.getChildren().addAll(loginPane);
                break;

            case LOGIN:
                this.getChildren().clear();
                this.getChildren().addAll(startLoginPane);
                break;

            case LOGOUT:
                this.getChildren().clear();
                this.getChildren().addAll(logoutPane);
                break;

            default:
                this.getChildren().clear();
                this.getChildren().addAll(startLoginPane);
                break;

        }
    }
}
