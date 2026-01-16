package com.codeit.closet.module.recommendation.config;

import com.codeit.closet.module.cloth.entity.ClothType;
import java.util.EnumMap;
import java.util.Map;

/**
 * 온도 구간별 의상 타입 적합도 설정 (0-40점)
 * - FREEZING(≤4°C), COLD(5-9), COOL(10-16), MILD(17-19), WARM(20-27), HOT(≥28)
 */
public class TemperatureRangeConfig {

    /**
     * 온도 구간 enum
     */
    public enum TemperatureRange {
        FREEZING(-999, 4),    // 4도 이하: 매우 추움 (한파)
        COLD(5, 9),           // 5-9도: 추움
        COOL(10, 16),         // 10-16도: 쌀쌀함 (간절기)
        MILD(17, 19),         // 17-19도: 선선함
        WARM(20, 27),         // 20-27도: 따뜻함
        HOT(28, 999);         // 28도 이상: 더움

        private final int minTemp;
        private final int maxTemp;

        TemperatureRange(int minTemp, int maxTemp) {
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
        }

        public int getMinTemp() {
            return minTemp;
        }

        public int getMaxTemp() {
            return maxTemp;
        }

        /**
         * 온도에 해당하는 구간 반환
         */
        public static TemperatureRange fromTemperature(double temperature) {
            int temp = (int) Math.round(temperature);
            for (TemperatureRange range : values()) {
                if (temp >= range.minTemp && temp <= range.maxTemp) {
                    return range;
                }
            }
            return temp < 0 ? FREEZING : HOT;
        }
    }

    // 온도 구간별 의상 타입 적합도 (0-100)
    // 높을수록 해당 온도에 적합한 의상 타입

    /**
     * 온도에 따른 의상 타입별 적합도 점수 반환
     * @param temperature 현재 온도
     * @return 의상 타입별 적합도 맵 (0-40점 범위로 정규화)
     */
    public static Map<ClothType, Integer> getSuitableClothTypes(double temperature) {
        TemperatureRange range = TemperatureRange.fromTemperature(temperature);
        return getSuitableClothTypesForRange(range);
    }

    /**
     * 온도 구간에 따른 의상 타입별 적합도 반환
     */
    private static Map<ClothType, Integer> getSuitableClothTypesForRange(TemperatureRange range) {
        Map<ClothType, Integer> suitability = new EnumMap<>(ClothType.class);

        // 기본값 설정 (모든 타입 중간 점수)
        for (ClothType type : ClothType.values()) {
            suitability.put(type, 20);
        }

        switch (range) {
            case FREEZING -> {
                // 4도 이하: 두꺼운 패딩, 울코트, 기모, 목도리, 장갑
                suitability.put(ClothType.OUTER, 40);    // 아우터 필수
                suitability.put(ClothType.TOP, 35);      // 따뜻한 상의
                suitability.put(ClothType.BOTTOM, 30);   // 기모 바지
                suitability.put(ClothType.SCARF, 35);    // 목도리 강추
                suitability.put(ClothType.HAT, 30);      // 모자/비니
                suitability.put(ClothType.DRESS, 10);    // 원피스 비추
            }
            case COLD -> {
                // 5-9도: 코트, 경량 패딩, 니트, 기모바지
                suitability.put(ClothType.OUTER, 38);
                suitability.put(ClothType.TOP, 35);
                suitability.put(ClothType.BOTTOM, 30);
                suitability.put(ClothType.SCARF, 28);
                suitability.put(ClothType.DRESS, 15);
            }
            case COOL -> {
                // 10-16도: 트렌치코트, 야상, 자켓 (간절기)
                suitability.put(ClothType.OUTER, 32);    // 아우터 선택적
                suitability.put(ClothType.TOP, 38);      // 상의 중심
                suitability.put(ClothType.BOTTOM, 32);
                suitability.put(ClothType.DRESS, 28);
            }
            case MILD -> {
                // 17-19도: 후드티, 스웻셔츠, 얇은 니트
                suitability.put(ClothType.OUTER, 22);    // 가벼운 겉옷
                suitability.put(ClothType.TOP, 40);      // 상의 중심
                suitability.put(ClothType.BOTTOM, 35);
                suitability.put(ClothType.DRESS, 32);
            }
            case WARM -> {
                // 20-27도: 반팔, 얇은 긴팔, 가디건
                suitability.put(ClothType.OUTER, 15);    // 아우터 거의 불필요
                suitability.put(ClothType.TOP, 40);
                suitability.put(ClothType.BOTTOM, 35);
                suitability.put(ClothType.DRESS, 38);    // 원피스 적합
            }
            case HOT -> {
                // 28도 이상: 민소매, 반바지, 린넨
                suitability.put(ClothType.OUTER, 5);     // 아우터 비추
                suitability.put(ClothType.TOP, 40);      // 시원한 상의
                suitability.put(ClothType.BOTTOM, 38);   // 반바지/통바지
                suitability.put(ClothType.DRESS, 40);    // 원피스 적합
                suitability.put(ClothType.SCARF, 5);     // 목도리 비추
            }
        }

        return suitability;
    }

    /**
     * 특정 의상 타입이 해당 온도에서 필수인지 여부
     */
    public static boolean isRequiredType(ClothType type, double temperature) {
        TemperatureRange range = TemperatureRange.fromTemperature(temperature);

        // 아우터는 추운 날씨에 필수
        if (type == ClothType.OUTER) {
            return range == TemperatureRange.FREEZING || range == TemperatureRange.COLD;
        }

        // TOP과 BOTTOM은 항상 필수 (DRESS로 대체 가능)
        return type == ClothType.TOP || type == ClothType.BOTTOM;
    }

    /**
     * 온도 구간에 대한 설명 반환
     */
    public static String getTemperatureDescription(double temperature) {
        TemperatureRange range = TemperatureRange.fromTemperature(temperature);
        return switch (range) {
            case FREEZING -> "한파";
            case COLD -> "추움";
            case COOL -> "쌀쌀함";
            case MILD -> "선선함";
            case WARM -> "따뜻함";
            case HOT -> "더움";
        };
    }
}
