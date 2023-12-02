package com.instream.tenant.domain.admin.admin.domain.request;

public record AdminSignInRequest(
        String account,

        String password
) {
}
