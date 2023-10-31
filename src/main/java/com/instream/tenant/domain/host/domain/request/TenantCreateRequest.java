package com.instream.tenant.domain.host.domain.request;

public record TenantCreateRequest(
        String account,

        String password,

        String name,

        String phoneNumber
) {
}
