package com.instream.tenant.domain.application.infra.mapper;

import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.domain.dto.ApplicationWithApiKeyDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.domain.entity.ApplicationSessionEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ApplicationMapper {
    ApplicationMapper INSTANCE = Mappers.getMapper(ApplicationMapper.class);


    @Mapping(target = "createdAt", source = "application.createdAt")
    @Mapping(target = "session", source = "applicationSession", qualifiedByName = "applicationSessionToSession")
    ApplicationWithApiKeyDto applicationAndSessionEntityToDto(ApplicationEntity application, ApplicationSessionEntity applicationSession);

    @Named("applicationSessionToSession")
    default ApplicationSessionDto applicationSessionToSession(ApplicationSessionEntity applicationSession) {
        return ApplicationSessionMapper.INSTANCE.entityToDto(applicationSession);
    }
}