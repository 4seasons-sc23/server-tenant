package com.instream.tenant.domain.sms.domain.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class VerifyAuthNumberRequestDto {
  @Schema(example = "010-1234-1234", description = "사용자 전화번호")
  private String userPhoneNum;

  @Schema(example = "1234", description = "사용자가 입력한 인증번호")
  private String authNumber;


}
