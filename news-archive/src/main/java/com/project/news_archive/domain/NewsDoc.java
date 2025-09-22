package com.project.news_archive.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true) // 클래스에 정의되지 않은 필드는 무시
public class NewsDoc {

    private String id;
    private String title;
    private String content;
    private String summary;
    private String url;
    private String source;
    private String lang;

    @JsonProperty("source_type") // JSON의 source_type 필드를 sourceType에 매핑
    private String sourceType;

    @JsonProperty("published_at") // JSON의 published_at 필드를 publishedAt에 매핑
    private String publishedAt;

}