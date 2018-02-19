package com.clocktower.teamcity.api.context.impl;

import com.clocktower.teamcity.api.exceptions.TeamCityException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class RestService {
    private final String teamCityUrl;

    private final HttpClient httpClient;
    private final ResponseParser responseParser;

    public RestService(String teamCityUrl) {
        this(teamCityUrl, HttpClients.createDefault(), new ResponseParser());
    }

    RestService(String teamCityUrl, HttpClient httpClient, ResponseParser responseParser) {
        this.teamCityUrl = teamCityUrl;
        this.httpClient = httpClient;
        this.responseParser = responseParser;
    }

    public String getTeamCityUrl() {
        return teamCityUrl;
    }

    public <T> T sendGetRequest(String resourcePath, Class<T> responseClass) {
        String url = createUrl(resourcePath);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());

        HttpResponse response;
        try {
            response = httpClient.execute(httpGet);
        } catch (IOException e) {
            throw new TeamCityException("Request to TeamCity instance has failed", e);
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new TeamCityException("Request to TeamCity resource returned with not OK status code");
        }

        return responseParser.parseJsonResponse(response, responseClass);
    }

    private String createUrl(String resourcePath) {
        return teamCityUrl + "/guestAuth" + "/app/rest" + resourcePath;
    }
}
