package com.infy.bankingApplication.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infy.bankingApplication.dto.KafkaDltMessageResponse;
import com.infy.bankingApplication.dto.KafkaDltRetryRequest;
import com.infy.bankingApplication.dto.KafkaDltRetryResponse;
import com.infy.bankingApplication.dto.KafkaDltSummaryResponse;
import com.infy.bankingApplication.service.KafkaDltMessageService;
import com.infy.bankingApplication.service.KafkaDltRetryService;
import com.infy.bankingApplication.service.KafkaDltSummaryService;

@RestController
@RequestMapping("/api/admin/kafka/dlt")
public class KafkaDltMessageController {
	
	private final KafkaDltMessageService messageService;
    private final KafkaDltSummaryService summaryService;
    private final KafkaDltRetryService retryService;

    public KafkaDltMessageController(
            KafkaDltMessageService messageService,
            KafkaDltSummaryService summaryService,
            KafkaDltRetryService retryService) {

        this.messageService = messageService;
        this.summaryService = summaryService;
        this.retryService = retryService;
    }

    @GetMapping("/messages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<KafkaDltMessageResponse>> getDltMessages(
            @RequestParam(defaultValue = "10") int limit) {

        if (limit < 1 || limit > 100) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                messageService.getDltMessages(limit));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KafkaDltSummaryResponse> getDltSummary() {

        return ResponseEntity.ok(
                summaryService.getDltSummary());
    }

    @PostMapping("/retry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KafkaDltRetryResponse> retryDltMessage(
            @RequestBody KafkaDltRetryRequest request) {

        if (request == null
                || request.getPartition() < 0
                || request.getOffset() < 0) {

            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                retryService.retryMessage(
                        request.getPartition(),
                        request.getOffset()));
    }

}
