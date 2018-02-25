package com.clocktower.teamcity.desktop.ui;

import com.clocktower.teamcity.api.context.Authorization;
import com.clocktower.teamcity.api.context.BuildType;
import com.clocktower.teamcity.api.context.Context;
import com.clocktower.teamcity.api.context.Project;
import com.clocktower.teamcity.desktop.domain.ApplicationState;
import com.clocktower.teamcity.desktop.domain.SettingsManager;
import com.google.common.base.Strings;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.StatusBar;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.clocktower.teamcity.desktop.domain.ApplicationConstants.APPLICATION_TITLE;

public class MainWindow {

    private Context context;
    private SettingsManager settingsManager;
    private ApplicationState state = ApplicationState.UNINITIALISED;

    private final Stage primaryStage;
    private MenuBar menuBar;
    private MaskerPane maskerPane;
    private TreeView<String> treeView;
    private StatusBar statusBar;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public MainWindow(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createUi();
    }

    public void show() {
        primaryStage.show();
        initializeAfterShow();
    }

    private void createUi() {
        treeView = new TreeView<>();
        treeView.setShowRoot(false);

        maskerPane = new MaskerPane();
        maskerPane.setVisible(false);

        StackPane centralPane = new StackPane();
        centralPane.getChildren().addAll(treeView, maskerPane);

        MenuBar menuBar = createMenuBar();

        statusBar = new StatusBar();

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(centralPane);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 400, 500);

        primaryStage.setTitle(APPLICATION_TITLE);
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(this::onCloseRequest);

//        primaryStage.addEventHandler(EventType.ROOT, event -> {
//            System.out.println("Event: " + event.getEventType());
//            System.out.println(event);
//        });
    }

    private MenuBar createMenuBar() {
        MenuItem settingsMenuItem = new MenuItem("Settings");
        settingsMenuItem.setOnAction(event -> showSettingsDialog());

        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(event -> exitApplication());

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().add(settingsMenuItem);
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(exitMenuItem);

        menuBar = new MenuBar();
        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }

    private void initializeAfterShow() {
        loadSettings();
        loadContext();
    }

    private void loadSettings() {
        settingsManager = new SettingsManager();
        boolean success = settingsManager.load();
        if (!success) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Application was unable to load settings.\n" +
                    "Default settings will be used");
            alert.setTitle(APPLICATION_TITLE);
            alert.setHeaderText(null);
            alert.initOwner(primaryStage);
            alert.showAndWait();
        }
    }

    private void setState(ApplicationState state) {
        this.state = state;
        switch (state) {
            case UNINITIALISED: {
                maskerPane.setVisible(false);
                menuBar.setDisable(false);
                treeView.setDisable(false);
                treeView.setRoot(generateUninitializedTree());
                statusBar.setText("");
                break;
            }
            case PROCESSING: {
                maskerPane.setVisible(true);
                menuBar.setDisable(true);
                treeView.setDisable(false);
                statusBar.setText("Running operation");
                break;
            }
            case READY: {
                maskerPane.setVisible(false);
                menuBar.setDisable(false);
                treeView.setDisable(false);
                statusBar.setText("Connected to " + settingsManager.getSettings().getTeamcityUrl());
                break;
            }
            case FAILED: {
                maskerPane.setVisible(false);
                menuBar.setDisable(false);
                treeView.setDisable(true);
                statusBar.setText("Operation failed on " + settingsManager.getSettings().getTeamcityUrl());
            }
        }
    }

    private void loadContext() {
        String teamCityUrl = settingsManager.getSettings().getTeamcityUrl();
        if (Strings.isNullOrEmpty(teamCityUrl)) {
            setState(ApplicationState.UNINITIALISED);
        } else {
            //context = new Context("https://teamcity.jetbrains.com");
            context = new Context(teamCityUrl, Authorization.guest());
            treeView.setRoot(null);
            launchTask("Loading context", () -> context.load());
        }
    }

    private void launchTask(String taskName, Runnable runnable) {
        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runnable.run();
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            updateTreeView();
            setState(ApplicationState.READY);
        });
        task.setOnFailed(event -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Operation has failed. Please check that the TeamCity server is accessible.");
            alert.setTitle(APPLICATION_TITLE);
            alert.setHeaderText(taskName);
            alert.initOwner(primaryStage);
            alert.showAndWait();
            setState(ApplicationState.FAILED);
            //task.getException().printStackTrace();
        });
        maskerPane.setText(taskName);
        setState(ApplicationState.PROCESSING);
        executorService.submit(task);
    }

    private void updateTreeView() {
        TreeItem<String> rootItem = generateTree();
        treeView.setRoot(rootItem);
    }

    private TreeItem<String> generateUninitializedTree() {
        TreeItem<String> root = new TreeItem<>("Root");
        root.getChildren().add(new TreeItem<>("Configure TeamCity URL in the Settings dialog (see File | Settings)"));
        return root;
    }

    private TreeItem<String> generateTree() {
        Project rootProject = context.getRootProject();
        return generateProjectTreeItem(rootProject);
    }

    private TreeItem<String> generateProjectTreeItem(Project project) {
        TreeItem<String> item = new TreeItem<>(project.getName());
        for (Project childProject : project.getChildProjects()) {
            TreeItem<String> childItem = generateProjectTreeItem(childProject);
            item.getChildren().add(childItem);
        }
        for (BuildType childBuildType : project.getChildBuildTypes()) {
            TreeItem<String> childItem = generateBuildTypeTreeItem(childBuildType);
            item.getChildren().add(childItem);
        }
        return item;
    }

    private TreeItem<String> generateBuildTypeTreeItem(BuildType buildType) {
        return new TreeItem<>(buildType.getName());
    }

    private void onCloseRequest(WindowEvent event) {
        event.consume();
        exitApplication();
    }

    private void exitApplication() {
        executorService.shutdown();
        primaryStage.close();
    }

    private void showSettingsDialog() {
        SettingsDialog settingsDialog = new SettingsDialog(primaryStage, settingsManager);
        settingsDialog.showAndWait();
        if (context == null || !Objects.equals(context.getTeamCityUrl(), settingsManager.getSettings().getTeamcityUrl())) {
            loadContext();
        }
    }
}
