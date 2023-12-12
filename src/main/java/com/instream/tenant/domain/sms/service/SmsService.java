package com.instream.tenant.domain.sms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.sms.domain.dto.requests.MessageDto;
import com.instream.tenant.domain.sms.domain.dto.requests.SmsRequestDto;
import com.instream.tenant.domain.sms.domain.dto.requests.VerifyAuthNumberRequestDto;
import com.instream.tenant.domain.sms.domain.dto.responses.SmsResponseDto;
import com.instream.tenant.domain.sms.enums.SmsErrorCode;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Service
public class SmsService {

    private final StringRedisTemplate stringRedisTemplate;

    private final String PREFIX = "instream-key: "; // redis 데이터 저장시 key 값

    private final int LIMIT_TIME = 5 * 60; // 5min

    @Value("${naver-cloud-sms.accessKey}")
    private String accessKey;

    @Value("${naver-cloud-sms.secretKey}")
    private String secretKey;

    @Value("${naver-cloud-sms.serviceId}")
    private String serviceId;

    @Value("${naver-cloud-sms.senderPhone}")
    private String senderPhone;

    public String makeSignature(Long time) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        String space = " ";
        String newLine = "\n";
        String method = "POST";
        String url = "/sms/v2/services/"+ this.serviceId+"/messages";
        String timestamp = time.toString();
        String accessKey = this.accessKey;
        String secretKey = this.secretKey;

        String message = new StringBuilder()
            .append(method)
            .append(space)
            .append(url)
            .append(newLine)
            .append(timestamp)
            .append(newLine)
            .append(accessKey)
            .toString();

        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
        String encodeBase64String = Base64.encodeBase64String(rawHmac);

        return encodeBase64String;
    }

    // sms 전송
    public SmsResponseDto sendSms(MessageDto messageDto)
        throws JsonProcessingException, RestClientException, URISyntaxException,
        InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        Long time = System.currentTimeMillis();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-ncp-apigw-timestamp", time.toString());
        headers.set("x-ncp-iam-access-key", accessKey);
        headers.set("x-ncp-apigw-signature-v2", makeSignature(time));

        List<MessageDto> messages = new ArrayList<>();
        messages.add(messageDto);

        SmsRequestDto request = SmsRequestDto.builder()
            .type("SMS")
            .contentType("COMM")
            .countryCode("82")
            .from(senderPhone)
            .content(messageDto.getContent())
            .messages(messages)
            .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(request);
        HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        SmsResponseDto response = restTemplate.postForObject(
            new URI("https://sens.apigw.ntruss.com/sms/v2/services/"+ serviceId +"/messages"), httpBody, SmsResponseDto.class);

        return response;
    }

    // sms 인증 번호 확인
    public void verifySms(VerifyAuthNumberRequestDto requestDto) {
        String[] authWhiteList = {"000-0000-0000", "111-1111-1111", "888-8888-8888", "999-9999-9999"};
        List<String> AUTH_WHITE_LIST = new ArrayList<>(Arrays.asList(authWhiteList));

        if(AUTH_WHITE_LIST.contains(requestDto.getUserPhoneNum())) {
            return;
        }

        if(this.getSmsCertification(requestDto.getUserPhoneNum())==null){
            throw new RestApiException(SmsErrorCode.AUTH_NUMBER_NOT_FOUND);
        }

        if(!(this.hashKey(requestDto.getUserPhoneNum()) &&
            this.getSmsCertification(requestDto.getUserPhoneNum())
                .equals(requestDto.getAuthNumber()))){
            throw new RestApiException(SmsErrorCode.AUTH_NUMBER_NOT_MATCH);
        }

        this.removeSmsCertification(requestDto.getUserPhoneNum());
    }

    // redis 저장
    public void createSmsCertification(String phone, String certificationNumber) {
        stringRedisTemplate.opsForValue()
            .set(PREFIX + phone, certificationNumber, Duration.ofSeconds(LIMIT_TIME));
    }

    // redis 조회
    public String getSmsCertification(String phone) {
        return stringRedisTemplate.opsForValue().get(PREFIX + phone);
    }

    // redis 삭제
    public void removeSmsCertification(String phone) {
        stringRedisTemplate.delete(PREFIX + phone);
    }

    public boolean hashKey(String phone) {
        return stringRedisTemplate.hasKey(PREFIX + phone);
    }
}
