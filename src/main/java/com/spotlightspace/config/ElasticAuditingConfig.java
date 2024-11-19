package com.spotlightspace.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.EnableElasticsearchAuditing;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@Configuration
@EnableElasticsearchAuditing
@EnableElasticsearchRepositories(basePackages = "com.spotlightspace.core.event.repository")
public class ElasticAuditingConfig {
}
