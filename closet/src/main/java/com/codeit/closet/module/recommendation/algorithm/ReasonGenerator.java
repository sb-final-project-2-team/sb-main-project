package com.codeit.closet.module.recommendation.algorithm;

import com.codeit.closet.module.cloth.entity.Cloth;
import com.codeit.closet.module.weather.entity.PrecipitationType;
import com.codeit.closet.module.weather.entity.WeatherData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 추천 이유 문구 생성 컴포넌트
 * 온도, 강수 상태, 점수에 따른 자연스러운 추천 이유 생성
 */
@Component
@RequiredArgsConstructor
public class ReasonGenerator {

    private final PrecipitationClothMatcher precipitationClothMatcher;

    /**
     * 추천 이유 문구 생성
     * @param outfit 추천 코디 조합
     * @param weather 날씨 데이터
     * @param score 추천 점수
     * @return 추천 이유 문자열
     */
    public String generateReason(List<Cloth> outfit, WeatherData weather, int score) {
        StringBuilder reason = new StringBuilder();

        double temp = weather.getTemperatureCurrent();

        // 온도 기반 이유
        reason.append(getTemperatureReason(temp));

        // 강수 기반 이유 추가
        String precipReason = getPrecipitationReason(weather.getPrecipitationType());
        if (!precipReason.isEmpty()) {
            reason.append(" ").append(precipReason);
        }

        // 점수 기반 추가 문구
        if (score >= 80) {
            reason.append(" 오늘 날씨에 딱 맞는 코디입니다!");
        } else if (score >= 60) {
            reason.append(" 오늘 날씨에 잘 어울리는 코디예요.");
        }

        return reason.toString();
    }

    /**
     * 온도 기반 추천 이유
     */
    private String getTemperatureReason(double temp) {
        if (temp <= 4) {
            return "한파가 예상되어 따뜻한 아우터와 보온 아이템을 추천드립니다.";
        } else if (temp <= 9) {
            return "쌀쌀한 날씨에 어울리는 따뜻한 레이어드 코디입니다.";
        } else if (temp <= 16) {
            return "선선한 날씨에 적합한 가벼운 겉옷 스타일입니다.";
        } else if (temp <= 22) {
            return "포근한 날씨에 어울리는 편안한 코디입니다.";
        } else if (temp <= 27) {
            return "따뜻한 날씨에 맞는 시원한 스타일입니다.";
        } else {
            return "무더운 날씨에 시원하게 입을 수 있는 코디입니다.";
        }
    }

    /**
     * 강수 기반 추천 이유
     */
    private String getPrecipitationReason(PrecipitationType precipType) {
        if (precipType == null || precipType == PrecipitationType.NONE) {
            return "";
        }

        return switch (precipType) {
            case RAIN -> "비 예보가 있어 방수 아이템을 고려해주세요.";
            case SNOW -> "눈이 예상되어 보온과 방수에 신경 쓴 코디입니다.";
            case RAIN_SNOW -> "비와 눈이 예상되어 방수와 보온을 모두 챙긴 코디입니다.";
            case SHOWER -> "소나기 예보가 있으니 가벼운 우비나 방수 자켓을 챙기세요.";
            default -> "";
        };
    }

    /**
     * 간단한 추천 이유 생성 (한 줄)
     */
    public String generateShortReason(WeatherData weather, int score) {
        double temp = weather.getTemperatureCurrent();
        String tempDesc = getTemperatureDescription(temp);

        if (precipitationClothMatcher.hasPrecipitation(weather.getPrecipitationType())) {
            String precipDesc = precipitationClothMatcher.getPrecipitationDescription(weather.getPrecipitationType());
            return String.format("%s 날씨와 %s에 적합한 코디입니다. (적합도 %d점)", tempDesc, precipDesc, score);
        }

        return String.format("%s 날씨에 적합한 코디입니다. (적합도 %d점)", tempDesc, score);
    }

    /**
     * 온도 설명 문구
     */
    private String getTemperatureDescription(double temp) {
        if (temp <= 4) return "매우 추운";
        if (temp <= 9) return "추운";
        if (temp <= 16) return "쌀쌀한";
        if (temp <= 22) return "선선한";
        if (temp <= 27) return "따뜻한";
        return "더운";
    }
}
