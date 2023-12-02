package com.instream.tenant.domain.billing.infra.mapper;

import com.instream.tenant.domain.application.domain.dto.ApplicationDto;
import com.instream.tenant.domain.application.domain.dto.ApplicationSessionDto;
import com.instream.tenant.domain.application.domain.dto.ApplicationWithApiKeyDto;
import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import com.instream.tenant.domain.application.domain.entity.ApplicationSessionEntity;
import com.instream.tenant.domain.application.infra.mapper.ApplicationSessionMapper;
import com.instream.tenant.domain.billing.domain.dto.BillingDto;
import com.instream.tenant.domain.billing.domain.entity.BillingEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(builder = @Builder(disableBuilder = true))
public interface BillingMapper {
    BillingMapper INSTANCE = Mappers.getMapper(BillingMapper.class);

    @Mapping(source = "billing.id", target = "id")
    @Mapping(source = "billing.status", target = "status")
    @Mapping(source = "billing.createdAt", target = "createdAt")
    @Mapping(source = "billing.updatedAt", target = "updatedAt")
    BillingDto billingAndApplicationToDto(BillingEntity billing, ApplicationDto application);
}