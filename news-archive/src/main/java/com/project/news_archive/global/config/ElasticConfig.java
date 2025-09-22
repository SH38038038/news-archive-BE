package com.project.news_archive.global.config;

// 1. import 클래스 변경
import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticConfig {

    @Bean
    // 2. 반환 타입 및 메서드 이름 변경 (메서드 이름은 필수는 아니지만 가독성을 위해 추천)
    public ElasticsearchAsyncClient elasticsearchAsyncClient() {
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http") // ElasticSearch 호스트/포트
        ).build();

        RestClientTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper()
        );

        // 3. 생성하는 클라이언트 클래스 변경
        return new ElasticsearchAsyncClient(transport);
    }
}