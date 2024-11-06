package com.spotlightspace.core.event.repository;

import com.spotlightspace.common.exception.ApplicationException;
import com.spotlightspace.core.event.domain.EventElastic;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.spotlightspace.common.exception.ErrorCode.EVENT_NOT_FOUND;

@Repository
public interface EventElasticRepository extends ElasticsearchRepository<EventElastic, Long>, EventElasticQueryRepository {

    Optional<EventElastic> findByIdAndIsDeletedFalse(Long id);
    default EventElastic findByIdOrElseThrow(Long id) {
        return findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ApplicationException(EVENT_NOT_FOUND));
    }
}
