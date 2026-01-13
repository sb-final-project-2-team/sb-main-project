package com.codeit.closet.module.cloth.service.impl;

import com.codeit.closet.module.cloth.dto.ClothDTO;
import com.codeit.closet.module.cloth.dto.ClothExtractionResult;
import com.codeit.closet.module.cloth.service.ClothExtractionService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

@Service
public class ClothExtractionServiceImpl implements ClothExtractionService {

    // 지원 도메인 목록
    private static final Set<String> ALLOWED_HOSTS = Set.of(
            "zigzag.kr",
            "www.musinsa.com",
            "29cm.co.kr"
    );

    @Override
    public ClothDTO extract(String rawUrl) {
        String url = validateAndTrim(rawUrl);
        URI uri = toUri(url);
        String host = normalizeHost(uri.getHost());

        if (!isAllowedHost(host)) { // 지원 도메인 체크
            throw new IllegalArgumentException("지원하지 않는 사이트입니다. : " + host);
        }

        ClothExtractionResult r = extractOgMeta(url);

        return new ClothDTO(
                null,
                null,
                r.name(),
                r.imageUrl(),
                null,
                null
        );
    }

    private ClothExtractionResult extractOgMeta(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .referrer("https://www.google.com")
                    .timeout(5000)
                    .get();

            String name = doc.select("meta[property=og:title]")
                    .attr("content");

            String imageUrl = doc.select("meta[property=og:image]")
                    .attr("content");

            if (name.isBlank()) {
                name = doc.title();
            }

            if (imageUrl.isBlank()) {
                throw new IllegalArgumentException("대표 이미지(og:image) 추출 실패");
            }

            return new ClothExtractionResult(name, imageUrl);
        } catch (IOException e) {
            throw new IllegalArgumentException("URL 접근/파싱 실패", e);
        }
    }

    private String validateAndTrim(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalArgumentException("url은 필수입니다.");
        }
        return rawUrl.trim();
    }

    private URI toUri(String url) {
        try {
            return URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("올바르지 않은 URL 형식입니다.");
        }
    }

    private String normalizeHost(String host) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("올바르지 않은 URL 형식입니다.");
        }
        return host.toLowerCase();
    }

    private boolean isAllowedHost(String host) {
        return ALLOWED_HOSTS.contains(host)
                || host.endsWith(".zigzag.kr")
                || host.endsWith(".musinsa.com")
                || host.endsWith(".29cm.co.kr");
    }
}