package com.instream.tenant.domain.sms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instream.tenant.domain.error.model.exception.RestApiException;
import com.instream.tenant.domain.sms.domain.requests.AuthNumberRequestDto;
import com.instream.tenant.domain.sms.domain.requests.MessageDto;
import com.instream.tenant.domain.sms.domain.requests.SmsRequestDto;
import com.instream.tenant.domain.sms.domain.requests.VerifyAuthNumberRequestDto;
import com.instream.tenant.domain.sms.domain.responses.SmsResponseDto;
import com.instream.tenant.domain.sms.infra.enums.SmsErrorCode;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class SmsService {

    private ReactiveRedisTemplate<String, String> redisTemplate;

    private final String PREFIX = "instream-key: "; // redis 데이터 저장시 key 값

    @Value("${naver-cloud-sms.accessKey}")
    private String accessKey;

    @Value("${naver-cloud-sms.secretKey}")
    private String secretKey;

    @Value("${naver-cloud-sms.serviceId}")
    private String serviceId;

    @Value("${naver-cloud-sms.senderPhone}")
    private String senderPhone;

    public SmsService(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Void> sendAuthNumber(AuthNumberRequestDto authNumberRequestDto, String authNum) {
        return Mono.just(MessageDto.builder()
                .to(authNumberRequestDto.userPhoneNum().replace("-", ""))
                .content("[In-Stream] 본인 확인 인증번호 [" + authNum + "]" + "입니다.")
                .build())
            .flatMap(messageDto -> {
                try {
                    return this.sendSms(messageDto);
                } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
                    return Mono.error(new RuntimeException());
                }
            })
            .then(this.createSmsCertification(authNumberRequestDto.userPhoneNum(), authNum))
            .then();
    }

    public Mono<String> makeSignature(Long time) {
        String space = " ";
        String newLine = "\n";
        String method = "POST";
        String url = "/sms/v2/services/"+ this.serviceId+"/messages";
        String timestamp = time.toString();
        String accessKey = this.accessKey;
        String secretKey = this.secretKey;

        String message = method
            + space
            + url
            + newLine
            + timestamp
            + newLine
            + accessKey;

        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        return Mono.fromCallable(() -> {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(signingKey);

                byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
                return Base64.getEncoder().encodeToString(rawHmac);
            });
    }

    // sms 전송
    public Mono<Void> sendSms(MessageDto messageDto)
        throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        Long time = System.currentTimeMillis();

        return makeSignature(time)
            .flatMap(signature -> {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("x-ncp-apigw-timestamp", time.toString());
                headers.set("x-ncp-iam-access-key", accessKey);
                headers.set("x-ncp-apigw-signature-v2", signature);
                List<MessageDto> messages = new ArrayList<>();
                messages.add(messageDto);

                SmsRequestDto request = SmsRequestDto.builder()
                    .type("SMS")
                    .contentType("COMM")
                    .countryCode("82")
                    .from(senderPhone)
                    .content(messageDto.content())
                    .messages(messages)
                    .build();

                ObjectMapper objectMapper = new ObjectMapper();
                String body;
                try {
                    body = objectMapper.writeValueAsString(request);
                } catch (JsonProcessingException e) {
                    return Mono.error(e);
                }

                return WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector())
                    .baseUrl(
                        "https://sens.apigw.ntruss.com/sms/v2/services/" + serviceId + "/messages")
                    .build()
                    .post()
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .body(BodyInserters.fromValue(body))
                    .retrieve()
                    .bodyToMono(SmsResponseDto.class)
                    .onErrorResume(throwable -> Mono.error(new RestApiException(SmsErrorCode.SMS_SEND_ERROR)));
            })
            .then();
    }

    // sms 인증 번호 확인
//    public Mono<Void> verifySms(VerifyAuthNumberRequestDto requestDto) {
//        if(this.getSmsCertification(requestDto.userPhoneNum())==null){
//            throw new RestApiException(SmsErrorCode.AUTH_NUMBER_NOT_FOUND);
//        }
//
//        if(!(this.hashKey(requestDto.userPhoneNum()) &&
//            this.getSmsCertification(requestDto.userPhoneNum())
//                .equals(requestDto.authNumber()))){
//            throw new RestApiException(SmsErrorCode.AUTH_NUMBER_NOT_MATCH);
//        }
//
//        this.removeSmsCertification(requestDto.userPhoneNum());
//    }

    public Mono<Void> verifySms(VerifyAuthNumberRequestDto requestDto) {
        return Mono.defer(() -> {
            String userPhoneNum = requestDto.userPhoneNum();
            Mono<String> savedCertificationMono = getSmsCertification(userPhoneNum);
            Mono<Boolean> hashMatches = hashKey(userPhoneNum);

            return savedCertificationMono
                .switchIfEmpty(Mono.error(new RestApiException(SmsErrorCode.AUTH_NUMBER_NOT_FOUND)))
                .zipWith(hashMatches)
                .flatMap(tuple -> {
                    String savedCertification = tuple.getT1();
                    boolean hashMatchesValue = tuple.getT2();
                    boolean authNumberMatches = savedCertification.equals(requestDto.authNumber());
                    System.out.println(savedCertification + " " + requestDto.authNumber());
                    if (hashMatchesValue && authNumberMatches) {
                        return removeSmsCertification(userPhoneNum);
                    } else {
                        return Mono.error(new RestApiException(SmsErrorCode.AUTH_NUMBER_NOT_MATCH));
                    }
                });
        });
    }

    // redis 저장
    private Mono<Void> createSmsCertification(String phone, String certificationNumber) {
        return redisTemplate.opsForValue()
            .set(PREFIX + phone, certificationNumber, Duration.ofMinutes(5))
            .then();
    }

    // redis 조회
    public Mono<String> getSmsCertification(String phone) {
        return redisTemplate.opsForValue().get(PREFIX + phone);
    }

    // redis 삭제
    public Mono<Void> removeSmsCertification(String phone) {
        return redisTemplate.delete(PREFIX + phone)
            .then();
    }

    public Mono<Boolean> hashKey(String phone) {
        return redisTemplate.hasKey(PREFIX + phone);
    }
}
