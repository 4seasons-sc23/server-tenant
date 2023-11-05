package com.instream.tenant.domain.participant.service;

import com.instream.tenant.domain.participant.domain.dto.ParticipantJoinDto;
import com.instream.tenant.domain.participant.domain.request.EnterToApplicationParticipantRequest;
import com.instream.tenant.domain.participant.repository.ParticipantJoinRepository;
import com.instream.tenant.domain.participant.repository.ParticipantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
public class ParticipantService {
    private final ParticipantRepository participantRepository;

    private final ParticipantJoinRepository participantJoinRepository;

    @Autowired
    public ParticipantService(ParticipantRepository participantRepository, ParticipantJoinRepository participantJoinRepository) {
        this.participantRepository = participantRepository;
        this.participantJoinRepository = participantJoinRepository;
    }

    Mono<ParticipantJoinDto> enterToApplication(String apiKey, String hostId, String participantId, EnterToApplicationParticipantRequest enterToApplicationParticipantRequest) {
        // TODO: 참가자 ID 암호화 로직 결정
        // TODO: participantId로 사용된 로직 추후 encryptedParticipantId로 수정

        return Mono.just(ParticipantJoinDto.builder()
                .build());
    }

    Mono<ParticipantJoinDto> leaveFromApplication(String apiKey, String hostId, String participantId, UUID applicationSessionId) {
        // TODO: 참가자 ID 암호화 로직 결정
        // TODO: participantId로 사용된 로직 추후 encryptedParticipantId로 수정
        return Mono.just(ParticipantJoinDto.builder()
                .build());
    }
}
