package com.clocktower.teamcity.desktop.ui;

import com.clocktower.teamcity.desktop.domain.SettingsManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static com.clocktower.teamcity.desktop.domain.ApplicationConstants.APPLICATION_TITLE;

public class SettingsDialog extends Stage {

    private final SettingsManager settingsManager;
    private TextField teamCityUrlTextField;

    public SettingsDialog(Stage parentStage, SettingsManager settingsManager) {
        this.settingsManager = settingsManager;

        initModality(Modality.WINDOW_MODAL);
        initOwner(parentStage);
        setTitle("Settings");

        createUi();
        populateFields();
    }

    private void createUi() {
        Label teamCityUrlLabel = new Label("TeamCity URL:");
        teamCityUrlLabel.setMinWidth(Region.USE_PREF_SIZE);
        teamCityUrlTextField = new TextField();
        teamCityUrlTextField.setPrefColumnCount(20);

        HBox teamCityUrlLayout = new HBox();
        teamCityUrlLayout.getChildren().addAll(teamCityUrlLabel, teamCityUrlTextField);
        teamCityUrlLayout.setSpacing(5);
        teamCityUrlLayout.setAlignment(Pos.BASELINE_LEFT);
        HBox.setHgrow(teamCityUrlLabel, Priority.NEVER);
        HBox.setHgrow(teamCityUrlTextField, Priority.ALWAYS);

        TitledPane serverSettingsTitledPane = new TitledPane("TeamCity Server", teamCityUrlLayout);
        serverSettingsTitledPane.setCollapsible(false);

        Region buttonBoxSpacer = new Region();

        Button okButton = new Button("OK");
        okButton.setMinWidth(75);
        okButton.setMinHeight(25);
        okButton.setOnAction(event -> onOkButtonClicked());

        Button cancelButton = new Button("Cancel");
        cancelButton.setMinWidth(75);
        cancelButton.setMinHeight(25);
        cancelButton.setOnAction(event -> onCancelButtonClicked());

        HBox buttonBox = new HBox();
        buttonBox.getChildren().addAll(buttonBoxSpacer, okButton, cancelButton);
        buttonBox.setSpacing(5);
        HBox.setHgrow(buttonBoxSpacer, Priority.ALWAYS);

        VBox vBoxLayout = new VBox();
        vBoxLayout.getChildren().addAll(serverSettingsTitledPane, buttonBox);
        vBoxLayout.setSpacing(10);

        VBox mainLayout = new VBox(vBoxLayout);
        VBox.setMargin(vBoxLayout, new Insets(10));

        Scene scene = new Scene(mainLayout);
        setScene(scene);
    }

    private void onOkButtonClicked() {
        saveAndClose();
    }

    private void onCancelButtonClicked() {
        close();
    }

    private void populateFields() {
        teamCityUrlTextField.setText(settingsManager.getSettings().getTeamcityUrl());
    }

    private void saveAndClose() {
        settingsManager.getSettings().setTeamcityUrl(teamCityUrlTextField.getText());
        trySaveSettings();
        close();
    }

    private void trySaveSettings() {
        boolean success = settingsManager.save();
        if (!success) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Application was unable to save settings.\n" +
                    "Changes to the settings made during this application run will most probably be lost.");
            alert.setTitle(APPLICATION_TITLE);
            alert.setHeaderText(null);
            alert.initOwner(this);
            alert.showAndWait();
        }
    }
}
