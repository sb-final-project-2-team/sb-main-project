package com.codeit.closet.module.weather.dto.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 기상청 API 공통 응답 구조
 */
@Data
public class KmaApiResponse {

    @JsonProperty("response")
    private Response response;

    @Data
    public static class Response {
        @JsonProperty("header")
        private Header header;

        @JsonProperty("body")
        private Body body;
    }

    @Data
    public static class Header {
        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMsg")
        private String resultMsg;
    }

    @Data
    public static class Body {
        @JsonProperty("dataType")
        private String dataType;

        @JsonProperty("items")
        private Items items;

        @JsonProperty("pageNo")
        private Integer pageNo;

        @JsonProperty("numOfRows")
        private Integer numOfRows;

        @JsonProperty("totalCount")
        private Integer totalCount;
    }

    @Data
    public static class Items {
        @JsonProperty("item")
        private List<Item> item;
    }

    @Data
    public static class Item {
        @JsonProperty("baseDate")
        private String baseDate;

        @JsonProperty("baseTime")
        private String baseTime;

        @JsonProperty("category")
        private String category;

        @JsonProperty("nx")
        private Integer nx;

        @JsonProperty("ny")
        private Integer ny;

        // 초단기실황 필드
        @JsonProperty("obsrValue")
        private String obsrValue;

        // 단기예보 필드
        @JsonProperty("fcstDate")
        private String fcstDate;

        @JsonProperty("fcstTime")
        private String fcstTime;

        @JsonProperty("fcstValue")
        private String fcstValue;
    }
}
