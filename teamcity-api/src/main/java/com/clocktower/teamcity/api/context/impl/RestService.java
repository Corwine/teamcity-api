package com.clocktower.teamcity.api.context.impl;

import com.clocktower.teamcity.api.context.impl.authorization.AuthorizationType;
import com.clocktower.teamcity.api.exceptions.FailedAuthorizationException;
import com.clocktower.teamcity.api.exceptions.TeamCityException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class RestService {
    private final String teamCityUrl;

    private final HttpClient httpClient;
    private final ResponseParser responseParser;
    private final AuthorizationType authorizationType;

    public RestService(String teamCityUrl, AuthorizationType authorizationType) {
        this(teamCityUrl, authorizationType, buildHttpClient(authorizationType), new ResponseParser());
    }

    RestService(String teamCityUrl, AuthorizationType authorizationType, HttpClient httpClient, ResponseParser responseParser) {
        this.teamCityUrl = teamCityUrl;
        this.authorizationType = authorizationType;
        this.httpClient = httpClient;
        this.responseParser = responseParser;
    }

    private static HttpClient buildHttpClient(AuthorizationType authorizationType) {
        HttpClientBuilder builder = HttpClients.custom();
        CredentialsProvider credentialsProvider = authorizationType.getCredentialsProvider();
        if (credentialsProvider != null) {
            builder.setDefaultCredentialsProvider(credentialsProvider);
        }
        return builder.build();
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

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                throw new FailedAuthorizationException("Request to TeamCity resource returned with UNAUTHORIZED error code. " +
                        "Please check authorization settings. Response:\n" + response);
            } else {
                throw new TeamCityException("Request to TeamCity resource returned with not OK status code. Response:\n" + response);
            }
        }

        return responseParser.parseJsonResponse(response, responseClass);
    }

    private String createUrl(String resourcePath) {
        return teamCityUrl + "/" + authorizationType.getPathPrefix() + "/app/rest" + resourcePath;
    }
}
