package com.clocktower.teamcity.desktop.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings {
    private String teamCityUrl;

    public String getTeamcityUrl() {
        return teamCityUrl;
    }

    public void setTeamcityUrl(String teamcityUrl) {
        this.teamCityUrl = teamcityUrl;
    }
}
