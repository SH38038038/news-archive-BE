package com.project.news_archive.repository;

import com.project.news_archive.domain.NewsDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends ElasticsearchRepository<NewsDoc, String> {
}

