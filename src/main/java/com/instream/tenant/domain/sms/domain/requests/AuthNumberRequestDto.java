package com.instream.tenant.domain.sms.domain.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;

public class AuthNumberRequestDto {
  @Schema(example = "010-1234-1234", description = "사용자 전화번호")
  private String userPhoneNum;

  public String getUserPhoneNum() {
    return userPhoneNum;
  }
}
