package com.clocktower.teamcity.api.context;

import com.clocktower.teamcity.api.context.impl.authorization.AuthorizationType;
import com.clocktower.teamcity.api.context.impl.authorization.GuestAuthorizationType;
import com.clocktower.teamcity.api.context.impl.authorization.HttpAuthorizationType;

public class Authorization {

    public static AuthorizationType guest() {
        return new GuestAuthorizationType();
    }

    public static AuthorizationType http(String username, String password) {
        return new HttpAuthorizationType(username, password);
    }
}
