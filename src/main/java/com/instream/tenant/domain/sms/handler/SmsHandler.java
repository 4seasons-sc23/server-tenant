package com.instream.tenant.domain.sms.handler;

import com.instream.tenant.domain.error.infra.enums.CommonHttpErrorCode;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.sms.domain.requests.AuthNumberRequestDto;
import com.instream.tenant.domain.sms.domain.requests.VerifyAuthNumberRequestDto;
import com.instream.tenant.domain.sms.service.SmsService;
import com.instream.tenant.domain.tenant.infra.enums.TenantErrorCode;
import java.util.Random;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class SmsHandler {
    private final SmsService smsService;

    String USER_PHONE_NUM_REGEXP = "^\\d{3}-\\d{4}-\\d{4}$";

    public SmsHandler(SmsService smsService) {
        this.smsService = smsService;
    }
    @NotNull
    public Mono<ServerResponse> sendAuthNumber(ServerRequest request)  {
        return request.bodyToMono(AuthNumberRequestDto.class)
            .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
            .flatMap(authNumberRequestDto -> {
                if (!Pattern.matches(USER_PHONE_NUM_REGEXP, authNumberRequestDto.userPhoneNum())) {
                    return Mono.error(new RestApiException(TenantErrorCode.USER_PHONE_NUM_FORMAT_ERROR));
                }
                Random random = new Random();
                // 4자리 랜덤 인증번호 생성
                String authNum = String.valueOf(random.nextInt(9999 - 1000 + 1) + 1000);
                return smsService.sendAuthNumber(authNumberRequestDto, authNum);
            })
            .then(ServerResponse.ok().build());
    }

    @NotNull
    public Mono<ServerResponse> verifyAuthNumber(ServerRequest request) {
        return request.bodyToMono(VerifyAuthNumberRequestDto.class)
            .onErrorMap(throwable -> new RestApiException(CommonHttpErrorCode.BAD_REQUEST))
            .flatMap(verifyAuthNumberRequestDto -> {
                if (!Pattern.matches(USER_PHONE_NUM_REGEXP, verifyAuthNumberRequestDto.userPhoneNum())) {
                    return Mono.error(new RestApiException(TenantErrorCode.USER_PHONE_NUM_FORMAT_ERROR));
                }
                return smsService.verifySms(verifyAuthNumberRequestDto);
            })
            .then(ServerResponse.ok().build());

    }

}
