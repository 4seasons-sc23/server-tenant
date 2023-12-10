package com.instream.tenant.domain.media.domain.request;

import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

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

    public Map<String, String> getQueryParams() {
        return extractQueryParams(tcurl);
    }

    private Map<String, String> extractQueryParams(String urlString) {
        Map<String, String> queryParams = new HashMap<>();

        // '?' 문자를 기준으로 쿼리 파라미터 부분을 추출
        String[] urlParts = urlString.split("\\?");
        if (urlParts.length > 1) {
            String query = urlParts[1];

            // '&'를 기준으로 각 key-value 쌍을 분리
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length > 1) {
                    queryParams.put(kv[0], kv[1]);
                } else {
                    queryParams.put(kv[0], ""); // 값이 없는 경우 빈 문자열 처리
                }
            }
        }

        return queryParams;
    }
}
