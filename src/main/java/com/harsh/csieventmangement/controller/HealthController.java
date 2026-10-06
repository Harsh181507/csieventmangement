package com.harsh.csieventmangement.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public String health() {
        return "CSI Event Management Backend Running";
    }

    // Lightweight check for Render / uptime monitors (no DB access)
    @GetMapping("/health")
    public Map<String, String> healthJson() {
        return Map.of("status", "UP");
    }
}
