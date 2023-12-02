package com.instream.tenant.domain.media.domain.request;

import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

public record NginxRtmpRequest(
        String addr,
        String app,
        String flashver,
        String swfurl,
        String tcurl,
        String pageurl,
        String name
) {
    public static Mono<NginxRtmpRequest> fromQueryParams(MultiValueMap<String, String> queryParams) {
        try {
            String addr = queryParams.getFirst("addr");
            String app = queryParams.getFirst("app");
            String flashver = queryParams.getFirst("flashver");
            String swfurl = queryParams.getFirst("swfurl");
            String tcurl = queryParams.getFirst("tcurl");
            String pageurl = queryParams.getFirst("pageurl");
            String name = queryParams.getFirst("name");

            return Mono.just(new NginxRtmpRequest(addr, app, flashver, swfurl, tcurl, pageurl, name));
        } catch (Exception e) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }
    }
}
