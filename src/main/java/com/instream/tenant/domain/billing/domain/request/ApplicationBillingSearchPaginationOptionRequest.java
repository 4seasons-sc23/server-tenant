package com.instream.tenant.domain.billing.domain.request;

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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class ApplicationBillingSearchPaginationOptionRequest extends PaginationOptionRequest {
    @JsonUnwrapped
    private final ApplicationBillingPaginationOption option;

    public ApplicationBillingSearchPaginationOptionRequest(int page, int size, List<SortOptionRequest> sort, boolean firstView, ApplicationBillingPaginationOption applicationBillingPaginationOption) {
        super(page, size, sort, firstView);
        this.option = applicationBillingPaginationOption;
    }

    public static Mono<ApplicationBillingSearchPaginationOptionRequest> fromQueryParams(MultiValueMap<String, String> queryParams) {
        try {

            List<SortOptionRequest> sortOptionRequestList = getSortOptionRequestList(queryParams);
            ApplicationBillingPaginationOption applicationBillingPaginationOption = ApplicationBillingPaginationOption.fromQueryParams(queryParams);
            int page = Integer.parseInt(Objects.requireNonNull(queryParams.getFirst("page")));
            int size = Integer.parseInt(Objects.requireNonNull(queryParams.getFirst("size")));
            Status status = Optional.ofNullable(queryParams.getFirst("status"))
                    .map(Status::fromCode)
                    .orElse(null);
            boolean invalidParameter = page < 0 || size <= 0 || status != null && (status.equals(Status.FORCE_STOPPED) || status.equals(Status.DELETED));

            if (invalidParameter) {
                throw new IllegalArgumentException();
            }

            boolean firstView = Boolean.parseBoolean(queryParams.getFirst("firstView"));

            ApplicationBillingSearchPaginationOptionRequest searchParams = new ApplicationBillingSearchPaginationOptionRequest(page, size, sortOptionRequestList, firstView, applicationBillingPaginationOption);

            return Mono.just(searchParams);
        } catch (Exception e) {
            return Mono.error(new RestApiException(CommonHttpErrorCode.BAD_REQUEST));
        }
    }
}
