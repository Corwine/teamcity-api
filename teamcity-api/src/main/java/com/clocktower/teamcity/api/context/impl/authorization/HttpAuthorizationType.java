package com.clocktower.teamcity.api.context.impl.authorization;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

public class HttpAuthorizationType implements AuthorizationType {

    private final String username;
    private final String password;

    public HttpAuthorizationType(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String getPathPrefix() {
        return "httpAuth";
    }

    @Override
    public CredentialsProvider getCredentialsProvider() {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        return credentialsProvider;
    }
}
