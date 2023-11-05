package com.instream.tenant.domain.host.domain.request;

public record TenantSignInRequest(
        String account,

        String password
) {
}
