package com.instream.tenant.domain.tenant.domain.request;

public record TenantSignInRequest(
        String account,

        String password
) {
}
