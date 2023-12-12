package com.instream.tenant.domain.sms.domain.dto.requests;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsRequestDto {
  String type;
  String contentType;
  String countryCode;
  String from;
  String content;
  List<MessageDto> messages;
}
