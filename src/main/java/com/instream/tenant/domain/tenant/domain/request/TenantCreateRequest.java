package com.instream.tenant.domain.tenant.domain.request;

public record TenantCreateRequest(
        String account,

        String password,

        String name,

        String phoneNumber
) {
}
