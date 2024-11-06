package com.spotlightspace.core.event.repository;

import com.spotlightspace.core.event.domain.EventElastic;
import com.spotlightspace.core.event.dto.response.GetEventElasticResponseDto;
import com.spotlightspace.core.event.dto.request.SearchEventRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EventElasticQueryRepositoryImpl implements EventElasticQueryRepository{
    private final ElasticsearchOperations operations;

    @Override
    public Page<GetEventElasticResponseDto> searchElasticEvents(
            SearchEventRequestDto requestDto, String type, Pageable pageable) throws IOException {

        CriteriaQuery query = new CriteriaQuery(new Criteria());

        if (requestDto.getTitle() != null) {
            query.addCriteria(Criteria.where("title").is(requestDto.getTitle()));
        }
        if (requestDto.getLocation() != null) {
            query.addCriteria(Criteria.where("location").is(requestDto.getLocation()));
        }
        if (requestDto.getCategory() != null) {
            query.addCriteria(Criteria.where("category").is(requestDto.getCategory()));
        }
        if (requestDto.getMaxPeople() != null) {
            query.addCriteria(Criteria.where("maxPeople").lessThanEqual(requestDto.getMaxPeople()));
        }
        if (requestDto.getRecruitmentStartAt() != null) {
            query.addCriteria(Criteria.where("recruitmentStartAt")
                    .greaterThanEqual(requestDto.getRecruitmentStartAt()));
        }
        if (requestDto.getRecruitmentFinishAt() != null) {
            query.addCriteria(Criteria.where("recruitmentFinishAt")
                    .lessThan(requestDto.getRecruitmentFinishAt()));
        }

        Sort sort = Sort.by(Sort.Order.desc("updatedAt"));

        if (type.equals("upprice")) {
            sort = Sort.by(Sort.Order.asc("price"));
        }
        if (type.equals("downprice")) {
            sort = Sort.by(Sort.Order.desc("price"));
        }
        if (type.equals("date")) {
            sort = Sort.by(Sort.Order.asc("recruitmentFinishAt"));
        }

        query.addSort(sort);
        query.setPageable(pageable);

        SearchHits<EventElastic> search = operations.search(query, EventElastic.class);

        List<GetEventElasticResponseDto> results = search.getSearchHits().stream()
                .map(hit -> GetEventElasticResponseDto.from(hit.getContent()))
                .collect(Collectors.toList());

        return new PageImpl<>(results, pageable, search.getTotalHits());
    }
}
