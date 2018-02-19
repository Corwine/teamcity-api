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
    private final String teamCityUri;

    private final HttpClient httpClient;
    private final ResponseParser responseParser;

    public RestService(String teamCityUri) {
        this(teamCityUri, HttpClients.createDefault(), new ResponseParser());
    }

    RestService(String teamCityUri, HttpClient httpClient, ResponseParser responseParser) {
        this.teamCityUri = teamCityUri;
        this.httpClient = httpClient;
        this.responseParser = responseParser;
    }

    public String getTeamCityUri() {
        return teamCityUri;
    }

    public <T> T sendGetRequest(String resourcePath, Class<T> responseClass) {
        String uri = createUri(resourcePath);
        HttpGet httpGet = new HttpGet(uri);
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

    private String createUri(String resourceUri) {
        return teamCityUri + "/guestAuth" + "/app/rest" + resourceUri;
    }
}
