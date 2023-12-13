package com.instream.tenant.domain.sms.domain.requests;

import java.util.List;
import lombok.Builder;
public record SmsRequestDto(
    String type,
    String contentType,
    String countryCode,
    String from,
    String content,
    List<MessageDto> messages
) {
  @Builder
  public SmsRequestDto{}
}
