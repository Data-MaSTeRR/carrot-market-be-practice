package com.carrot.market.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class LocationService {

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    @Value("${kakao.map.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LocationService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 위도/경도를 주소로 변환 (Reverse Geocoding)
     * Kakao Local API 사용
     */
    public String reverseGeocode(double latitude, double longitude) {
        try {
            logger.info("Reverse geocoding request - Latitude: {}, Longitude: {}", latitude, longitude);
            logger.debug("Using Kakao API Key: {}...", apiKey.substring(0, Math.min(10, apiKey.length())));

            // Kakao Reverse Geocoding API URL 생성
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://dapi.kakao.com/v2/local/geo/coord2address.json")
                    .queryParam("x", longitude)  // 경도
                    .queryParam("y", latitude)   // 위도
                    .toUriString();

            logger.debug("Request URL: {}", url);

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + apiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            logger.debug("API Response Status: {}", response.getStatusCode());
            logger.debug("API Response Body: {}", response.getBody());

            // 응답 파싱
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode documents = root.path("documents");

            if (documents.isArray() && documents.size() > 0) {
                JsonNode firstDocument = documents.get(0);

                String region1 = "";
                String region2 = "";
                String region3 = "";

                // 지번 주소에서 기본 정보 가져오기 (지번 주소가 더 정확함)
                JsonNode address = firstDocument.path("address");
                if (address != null && !address.isMissingNode()) {
                    region1 = address.path("region_1depth_name").asText("");  // 시/도
                    region2 = address.path("region_2depth_name").asText("");  // 구/군
                    region3 = address.path("region_3depth_name").asText("");  // 동/읍/면

                    logger.debug("Address info - region1: {}, region2: {}, region3: {}", region1, region2, region3);
                }

                // 도로명 주소에서 추가 정보 확인 (동 정보가 더 상세할 수 있음)
                JsonNode roadAddress = firstDocument.path("road_address");
                if (roadAddress != null && !roadAddress.isMissingNode()) {
                    String roadRegion1 = roadAddress.path("region_1depth_name").asText("");
                    String roadRegion2 = roadAddress.path("region_2depth_name").asText("");
                    String roadRegion3 = roadAddress.path("region_3depth_name").asText("");

                    logger.debug("Road address info - region1: {}, region2: {}, region3: {}", roadRegion1, roadRegion2, roadRegion3);

                    // 지번 주소에 동 정보가 없고 도로명 주소에 있으면 사용
                    if (region3.isEmpty() && !roadRegion3.isEmpty()) {
                        region3 = roadRegion3;
                    }
                }

                // 최종 주소 조합 (시/도 구/군 동)
                if (!region1.isEmpty() && !region2.isEmpty()) {
                    String finalAddress;
                    if (!region3.isEmpty()) {
                        finalAddress = String.format("%s %s %s", region1, region2, region3).trim();
                    } else {
                        finalAddress = String.format("%s %s", region1, region2).trim();
                    }
                    logger.info("Successfully converted to address: {}", finalAddress);
                    return finalAddress;
                }
            }

            logger.error("No address found in API response");
            throw new RuntimeException("주소를 찾을 수 없습니다.");

        } catch (Exception e) {
            logger.error("Error during reverse geocoding", e);
            throw new RuntimeException("주소 변환 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}