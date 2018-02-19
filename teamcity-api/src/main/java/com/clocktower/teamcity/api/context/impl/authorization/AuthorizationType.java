package com.clocktower.teamcity.api.context.impl.authorization;

import org.apache.http.client.CredentialsProvider;

public interface AuthorizationType {

    String getPathPrefix();

    CredentialsProvider getCredentialsProvider();
}
