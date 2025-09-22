package com.project.news_archive.service;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.project.news_archive.domain.NewsDoc;
import com.project.news_archive.domain.NewsSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final ElasticsearchAsyncClient elasticsearchAsyncClient;
    private static final String NEWS_INDEX = "news-*"; // 실제 Elasticsearch 인덱스 이름으로 변경하세요.

    /**
     * 전체 뉴스 문서를 동기적으로 조회합니다.
     * 주의: 이 메서드는 모든 문서를 메모리로 가져오므로, 데이터가 많을 경우 성능 문제를 일으킬 수 있습니다.
     * @return 뉴스 문서 리스트
     */
    public List<NewsDoc> getAllDocs() throws IOException {
        SearchResponse<NewsSource> response = elasticsearchAsyncClient.search(s -> s
                        .index(NEWS_INDEX)
                        .query(q -> q.matchAll(m -> m)),
                NewsSource.class
        ).join(); // 비동기 작업이 완료될 때까지 기다립니다.

        return response.hits().hits().stream()
                .map(hit -> hit.source().getDoc())
                .collect(Collectors.toList());
    }

    /**
     * ID를 기반으로 단일 뉴스 문서를 비동기적으로 조회합니다.
     * @param id 문서 ID
     * @return CompletableFuture를 통해 뉴스 문서를 반환
     */
    public CompletableFuture<NewsDoc> getNewsByIdAsync(String id) {
        return elasticsearchAsyncClient.get(g -> g
                        .index(NEWS_INDEX)
                        .id(id),
                NewsDoc.class
        ).thenApply(response -> {
            if (response.found()) {
                return response.source();
            } else {
                log.warn("News with id '{}' not found.", id);
                return null;
            }
        });
    }

    /**
     * 다양한 조건(키워드, 날짜, 정렬, 페이징)으로 뉴스를 비동기 검색합니다.
     * @param keyword 검색 키워드
     * @param startDate 검색 시작일 (YYYY-MM-DD)
     * @param endDate 검색 종료일 (YYYY-MM-DD)
     * @param sortField 정렬 필드
     * @param sortOrder 정렬 순서 (asc/desc)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return CompletableFuture를 통해 뉴스 문서 리스트를 반환
     */
    public CompletableFuture<List<NewsDoc>> searchNewsAsync(
            String keyword, String startDate, String endDate,
            String sortField, String sortOrder, int page, int size) {

        SearchRequest.Builder requestBuilder = new SearchRequest.Builder();
        requestBuilder.index(NEWS_INDEX);

        // 1. Bool 쿼리 생성을 위한 준비
        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        // 2. 키워드 쿼리 (must)
        if (StringUtils.hasText(keyword)) {
            mustQueries.add(
                    new Query.Builder().multiMatch(m -> m
                            .query(keyword)
                            .fields("doc.title", "doc.content", "doc.source") // 검색할 필드 지정
                    ).build()
            );
        }

        // 3. 날짜 범위 쿼리 (filter - 스코어에 영향을 주지 않아 더 효율적)
        if (StringUtils.hasText(startDate) || StringUtils.hasText(endDate)) {
            filterQueries.add(
                    new Query.Builder().range(r -> {
                        r.field("publishedDate"); // 날짜 필드 지정
                        if (StringUtils.hasText(startDate)) {
                            r.gte(JsonData.of(startDate));
                        }
                        if (StringUtils.hasText(endDate)) {
                            r.lte(JsonData.of(endDate));
                        }
                        return r;
                    }).build()
            );
        }

        // 4. 최종 쿼리 조합
        requestBuilder.query(q -> q
                .bool(b -> {
                    if (!mustQueries.isEmpty()) {
                        b.must(mustQueries);
                    }
                    if (!filterQueries.isEmpty()) {
                        b.filter(filterQueries);
                    }
                    // must와 filter가 모두 비어있을 경우, 모든 문서를 대상으로 함
                    if (mustQueries.isEmpty() && filterQueries.isEmpty()) {
                        b.must(m -> m.matchAll(ma -> ma));
                    }
                    return b;
                })
        );

        // 5. 페이징 설정
        requestBuilder.from(page * size).size(size);

        // 6. 정렬 설정
        SortOrder order = "asc".equalsIgnoreCase(sortOrder) ? SortOrder.Asc : SortOrder.Desc;
        requestBuilder.sort(s -> s.field(f -> f.field(sortField).order(order)));

        // 7. 비동기 검색 실행 및 결과 처리
        return elasticsearchAsyncClient
                .search(requestBuilder.build(), NewsSource.class)
                .thenApply(searchResponse -> {
                    return searchResponse.hits().hits().stream()
                            .map(hit -> hit.source().getDoc())
                            .collect(Collectors.toList());
                });
    }
}