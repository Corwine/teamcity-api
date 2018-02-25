package com.clocktower.teamcity.desktop;

import com.clocktower.teamcity.desktop.ui.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;

public class TeamCityDesktop extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        MainWindow mainWindow = new MainWindow(primaryStage);
        mainWindow.show();
    }
}
