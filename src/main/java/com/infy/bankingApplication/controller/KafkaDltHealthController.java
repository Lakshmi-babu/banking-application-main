package com.infy.bankingApplication.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.infy.bankingApplication.dto.KafkaDltHealthResponse;
import com.infy.bankingApplication.service.KafkaDltHealthService;

@RestController
@RequestMapping("/api/admin/kafka/dlt")
public class KafkaDltHealthController {

    private final KafkaDltHealthService healthService;

    public KafkaDltHealthController(KafkaDltHealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KafkaDltHealthResponse> checkDltHealth() {
        KafkaDltHealthResponse response = healthService.checkDltHealth();
        return ResponseEntity.ok(response);
    }
}
