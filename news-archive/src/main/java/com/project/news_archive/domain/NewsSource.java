package com.project.news_archive.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // doc 외 다른 최상위 필드들은 무시
public class NewsSource {

    // JSON의 "doc" 객체를 NewsDoc 클래스로 받음
    private NewsDoc doc;

}