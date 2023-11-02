package com.instream.tenant.domain.application;


import com.instream.tenant.domain.application.domain.entity.ApplicationEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface ApplicationRepository extends ReactiveCrudRepository<ApplicationEntity, UUID> {
}
