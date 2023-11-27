package com.instream.tenant.domain.participant.infra.mapper;

import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationSessionEntity;
import com.instream.tenant.domain.participant.domain.dto.ParticipantDto;
import com.instream.tenant.domain.participant.domain.entity.ParticipantEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ParticipantMapper {
    ParticipantMapper INSTANCE = Mappers.getMapper(ParticipantMapper.class);

    ParticipantDto entityToDto(ParticipantEntity entity);
}