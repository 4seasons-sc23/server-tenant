package com.instream.tenant.domain.sms.infra.enums;

import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum SmsErrorCode implements HttpErrorCode {
  AUTH_NUMBER_NOT_MATCH(HttpStatus.UNAUTHORIZED, "인증번호가 일치하지 않습니다."),
  AUTH_NUMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "인증번호 요청을 다시 해주세요."),
  MAKE_SIGNATURE_TYPE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Signature 생성 중 오류가 발생했습니다."),
  SMS_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SMS 전송 중 오류가 발생했습니다.");
  private final HttpStatus httpStatus;
  private final String message;

  @Override
  public String getCode() {
    return name();
  }
}