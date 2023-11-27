package com.instream.tenant.domain.participant.infra.mapper;

import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.domain.dto.ApplicationWithApiKeyDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.domain.entity.ApplicationSessionEntity;
import com.instream.tenant.domain.application.infra.mapper.ApplicationMapper;
import com.instream.tenant.domain.application.infra.mapper.ApplicationSessionMapper;
import com.instream.tenant.domain.participant.domain.dto.ParticipantDto;
import com.instream.tenant.domain.participant.domain.dto.ParticipantJoinDto;
import com.instream.tenant.domain.participant.domain.entity.ParticipantEntity;
import com.instream.tenant.domain.participant.domain.entity.ParticipantJoinEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ParticipantJoinMapper {
    ParticipantJoinMapper INSTANCE = Mappers.getMapper(ParticipantJoinMapper.class);

    @Mapping(source = "participantJoin.id", target = "id")
    @Mapping(source = "participantJoin.createdAt", target = "createdAt")
    @Mapping(source = "participantJoin.updatedAt", target = "updatedAt")
    @Mapping(source = "participant", target = "participant", qualifiedByName = "participantEntityToDto")
    @Mapping(source = "application", target = "application")
    ParticipantJoinDto participantAndParticipantJoinAndApplicationToDto(ParticipantEntity participant, ParticipantJoinEntity participantJoin, ApplicationDto application);

    @Named("participantEntityToDto")
    default ParticipantDto participantEntityToDto(ParticipantEntity participant) {
        return ParticipantMapper.INSTANCE.entityToDto(participant);
    }
}