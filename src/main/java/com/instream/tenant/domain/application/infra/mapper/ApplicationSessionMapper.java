package com.instream.tenant.domain.application.infra.mapper;

import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationSessionEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ApplicationSessionMapper {
    ApplicationSessionMapper INSTANCE = Mappers.getMapper(ApplicationSessionMapper.class);


    ApplicationSessionDto entityToDto(ApplicationSessionEntity entity);
}