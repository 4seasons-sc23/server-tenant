package com.instream.tenant.domain.sms.enums;

import com.instream.tenant.domain.error.infra.enums.HttpErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum SmsErrorCode implements HttpErrorCode {
  AUTH_NUMBER_NOT_MATCH(HttpStatus.UNAUTHORIZED, "인증번호가 일치하지 않습니다."),
  AUTH_NUMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "인증번호 요청을 다시 해주세요.");

  private final HttpStatus httpStatus;
  private final String message;

  @Override
  public String getCode() {
    return name();
  }
}