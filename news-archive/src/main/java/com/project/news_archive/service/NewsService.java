package com.project.news_archive.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.project.news_archive.domain.NewsDoc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final ElasticsearchClient esClient;

    public List<NewsDoc> getAllDocs() throws IOException {
        SearchResponse<Map> response = esClient.search(s -> s
                        .index("news-*")
                        .size(1000)
                        .query(q -> q.matchAll(m -> m)),
                Map.class
        );

        return response.hits().hits().stream()
                .map(hit -> convertToNewsDocFromSource((Map<String, Object>) hit.source()))
                .filter(doc -> doc != null)
                .collect(Collectors.toList());
    }

    private NewsDoc convertToNewsDocFromSource(Map<String, Object> sourceMap) {
        if (sourceMap == null) return null;

        Map<String, Object> docMap = (Map<String, Object>) sourceMap.get("doc");
        if (docMap == null) return null;

        NewsDoc doc = new NewsDoc();
        doc.setId((String) docMap.get("id"));
        doc.setTitle((String) docMap.get("title"));
        doc.setContent((String) docMap.get("content"));
        doc.setSummary((String) docMap.get("summary"));
        doc.setUrl((String) docMap.get("url"));
        doc.setSource((String) docMap.get("source"));
        doc.setSource_type((String) docMap.get("source_type"));
        doc.setPublished_at((String) docMap.get("published_at"));
        doc.setLang((String) docMap.get("lang"));
        return doc;
    }
}
