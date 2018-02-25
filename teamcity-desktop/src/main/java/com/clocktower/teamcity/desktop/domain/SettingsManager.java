package com.clocktower.teamcity.desktop.domain;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SettingsManager {
    private static final Path HOME_FOLDER_PATH = Paths.get(System.getProperty("user.home"));
    private static final String SETTINGS_FOLDER_NAME = ".teamcity-desktop";
    private static final String SETTINGS_FILE_NAME = "settings.json";

    private Settings settings = new Settings();

    public Settings getSettings() {
        return settings;
    }

    public boolean load() {
        Path settingsFilePath = HOME_FOLDER_PATH.resolve(SETTINGS_FOLDER_NAME).resolve(SETTINGS_FILE_NAME);
        if (!Files.exists(settingsFilePath)) {
            // File does not exist - not a problem, use default
            return true;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            settings = objectMapper.readValue(settingsFilePath.toFile(), Settings.class);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean save() {
        Path settingsFolderPath = HOME_FOLDER_PATH.resolve(SETTINGS_FOLDER_NAME);
        if (!Files.exists(settingsFolderPath)) {
            try {
                Files.createDirectory(settingsFolderPath);
            } catch (IOException e) {
                return false;
            }
        }

        Path settingsFilePath = settingsFolderPath.resolve(SETTINGS_FILE_NAME);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(settingsFilePath.toFile(), settings);
        } catch (IOException e) {
            return false;
        }

        return true;
    }
}
