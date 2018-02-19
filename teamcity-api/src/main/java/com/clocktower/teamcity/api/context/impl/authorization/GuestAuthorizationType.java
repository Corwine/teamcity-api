package com.clocktower.teamcity.api.context.impl.authorization;

import org.apache.http.client.CredentialsProvider;

public class GuestAuthorizationType implements AuthorizationType {

    @Override
    public String getPathPrefix() { return "guestAuth"; }

    @Override
    public CredentialsProvider getCredentialsProvider() { return null; }
}
