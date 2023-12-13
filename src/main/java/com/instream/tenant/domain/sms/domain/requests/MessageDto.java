package com.instream.tenant.domain.sms.domain.requests;

import lombok.Builder;
import lombok.Getter;

public record MessageDto(
    String to,
    String content
) {
  @Builder
  public  MessageDto{}
}
