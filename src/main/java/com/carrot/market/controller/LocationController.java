package com.carrot.market.controller;

import com.carrot.market.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * GPS 좌표를 주소로 변환
     */
    @GetMapping("/reverse-geocode")
    public ResponseEntity<?> reverseGeocode(
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {
        try {
            String address = locationService.reverseGeocode(latitude, longitude);

            Map<String, String> response = new HashMap<>();
            response.put("address", address);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}