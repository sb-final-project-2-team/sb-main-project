package com.codeit.closet.module.weather.dto.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 기상청 API Category 코드
 */
@Getter
@RequiredArgsConstructor
public enum KmaCategoryCode {
    // 초단기실황 (UltraSrtNcst)
    T1H("기온", "℃"),
    RN1("1시간 강수량", "mm"),
    UUU("동서바람성분", "m/s"),
    VVV("남북바람성분", "m/s"),
    REH("습도", "%"),
    PTY("강수형태", "코드값"),
    VEC("풍향", "deg"),
    WSD("풍속", "m/s"),

    // 단기예보 추가 필드 (VilageFcst)
    POP("강수확률", "%"),
    PCP("1시간 강수량", "범주"),
    SNO("1시간 신적설", "범주"),
    SKY("하늘상태", "코드값"),
    TMP("1시간 기온", "℃"),
    TMN("일 최저기온", "℃"),
    TMX("일 최고기온", "℃"),
    WAV("파고", "M");

    private final String description;
    private final String unit;
}
