package com.instream.tenant.domain.application.domain.request;

import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.common.domain.request.PaginationOptionRequest;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@Getter
public class ApplicationSessionSearchPaginationOptionRequest extends PaginationOptionRequest {
    @Schema(description = "어플리케이션 종류")
    private final ApplicationType type;

    public ApplicationSessionSearchPaginationOptionRequest(int page, int size, boolean firstView, ApplicationType type) {
        super(page, size, firstView);
        this.type = type;
    }

    public static Mono<ApplicationSessionSearchPaginationOptionRequest> fromQueryParams(MultiValueMap<String, String> queryParams) {
        try {
            int page = Integer.parseInt(Objects.requireNonNull(queryParams.getFirst("page")));
            int size = Integer.parseInt(Objects.requireNonNull(queryParams.getFirst("size")));

            if(page < 0 || size <= 0) {
                throw new IllegalArgumentException();
            }

            boolean firstView = Boolean.parseBoolean(queryParams.getFirst("firstView"));
            ApplicationType type = Optional.ofNullable(queryParams.getFirst("type"))
                    .map(ApplicationType::valueOf)
                    .orElse(null);


            ApplicationSessionSearchPaginationOptionRequest searchParams = new ApplicationSessionSearchPaginationOptionRequest(page, size, firstView, type);

            return Mono.just(searchParams);
        } catch (Exception e) {

            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }
    }
}
