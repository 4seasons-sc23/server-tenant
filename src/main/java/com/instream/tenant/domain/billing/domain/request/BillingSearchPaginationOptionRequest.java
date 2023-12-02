package com.instream.tenant.domain.billing.domain.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.instream.tenant.domain.application.infra.enums.ApplicationType;
import com.instream.tenant.domain.common.domain.request.PaginationOptionRequest;
import com.instream.tenant.domain.common.domain.request.SortOptionRequest;
import com.instream.tenant.domain.common.infra.enums.Status;
import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class BillingSearchPaginationOptionRequest extends PaginationOptionRequest {
    @Schema(description = "어플리케이션 ID", example = "80bd6328-76a7-11ee-b720-0242ac130003")
    private final UUID applicationId;

    @Schema(description = "어플리케이션 종류")
    private final ApplicationType type;

    @JsonUnwrapped
    private final ApplicationBillingPaginationOption option;

    public BillingSearchPaginationOptionRequest(int page, int size, List<SortOptionRequest> sort, boolean firstView, UUID applicationId, ApplicationType type, ApplicationBillingPaginationOption applicationBillingPaginationOption) {
        super(page, size, sort, firstView);
        this.applicationId = applicationId;
        this.type = type;
        this.option = applicationBillingPaginationOption;
    }

    public static Mono<BillingSearchPaginationOptionRequest> fromQueryParams(MultiValueMap<String, String> queryParams) {
        try {

            List<SortOptionRequest> sortOptionRequestList = getSortOptionRequestList(queryParams);
            ApplicationBillingPaginationOption applicationBillingPaginationOption = ApplicationBillingPaginationOption.fromQueryParams(queryParams);
            int page = Integer.parseInt(Objects.requireNonNull(queryParams.getFirst("page")));
            int size = Integer.parseInt(Objects.requireNonNull(queryParams.getFirst("size")));
            boolean invalidParameter = page < 0 || size <= 0;

            if (invalidParameter) {
                throw new IllegalArgumentException();
            }

            boolean firstView = Boolean.parseBoolean(queryParams.getFirst("firstView"));
            UUID applicationId = Optional.ofNullable(queryParams.getFirst("applicationId"))
                    .map(UUID::fromString)
                    .orElse(null);
            ApplicationType type = Optional.ofNullable(queryParams.getFirst("type"))
                    .map(ApplicationType::fromCode)
                    .orElse(null);
            BillingSearchPaginationOptionRequest searchParams = new BillingSearchPaginationOptionRequest(page, size, sortOptionRequestList, firstView, applicationId, type, applicationBillingPaginationOption);

            return Mono.just(searchParams);
        } catch (Exception e) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }
    }
}
