package com.spotlightspace.common.constant;

public abstract class JwtConstant {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final long TOKEN_ACCESS_TIME = 60 * 60 * 1000L;
    public static final long TOKEN_REFRESH_TIME = 60 * 60 * 12 * 1000L;
    public static final String USER_EMAIL = "email";
    public static final String USER_ROLE = "userRole";
}

