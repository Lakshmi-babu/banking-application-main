package com.infy.bankingApplication.controller;



import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;



import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.infy.bankingApplication.service.AuditLogExportService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infy.bankingApplication.entity.AuditLog;

import com.infy.bankingApplication.service.AuditLogService;



@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
	
	private final AuditLogService auditLogService;
	private final AuditLogExportService auditLogExportService;

    public AuditLogController(AuditLogService auditLogService, AuditLogExportService auditLogExportService) {
        this.auditLogService = auditLogService;
        this.auditLogExportService = auditLogExportService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AuditLog> getAllAuditLogs(
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC)
            Pageable pageable) {

        return auditLogService.getAllAuditLogs(pageable);
    }

    @GetMapping("/{accountNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AuditLog> getAuditLogsByAccount(
            @PathVariable String accountNumber,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC)
            Pageable pageable) {

        return auditLogService.getAuditLogsByAccount(
                accountNumber, pageable);
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AuditLog> getAuditLogsByDateRange(
            @RequestParam
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,

            @RequestParam
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate,

            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC)
            Pageable pageable) {

        return auditLogService.getAuditLogsByDateRange(
                startDate,
                endDate,
                pageable);
    }
    

@GetMapping("/filter")
@PreAuthorize("hasRole('ADMIN')")
public Page<AuditLog> getAuditLogsByFilters(
        @RequestParam String action,
        @RequestParam String status,

        @RequestParam
        @org.springframework.format.annotation.DateTimeFormat(
                iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startDate,

        @RequestParam
        @org.springframework.format.annotation.DateTimeFormat(
                iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endDate,

        @PageableDefault(
                size = 10,
                sort = "createdAt",
                direction = Sort.Direction.DESC)
        Pageable pageable) {

    return auditLogService.getAuditLogsByFilters(
            action,
            status,
            startDate,
            endDate,
            pageable);
}


@GetMapping("/user/{username}")
@PreAuthorize("hasRole('ADMIN')")
public Page<AuditLog> getAuditLogsByUsername(
        @PathVariable String username,

        @PageableDefault(
                size = 10,
                sort = "createdAt",
                direction = Sort.Direction.DESC)
        Pageable pageable) {

    return auditLogService.getAuditLogsByUsername(
            username,
            pageable);
}


@GetMapping("/user/{username}/account/{accountNumber}")
@PreAuthorize("hasRole('ADMIN')")
public Page<AuditLog> getAuditLogsByUsernameAndAccount(
        @PathVariable String username,
        @PathVariable String accountNumber,

        @PageableDefault(
                size = 10,
                sort = "createdAt",
                direction = Sort.Direction.DESC)
        Pageable pageable) {

    return auditLogService.getAuditLogsByUsernameAndAccount(
            username,
            accountNumber,
            pageable);
}

@GetMapping("/export/csv")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<byte[]> exportAuditLogsToCsv() throws IOException {

    List<AuditLog> auditLogs = auditLogService.getAllAuditLogs(
            org.springframework.data.domain.PageRequest.of(
                    0, Integer.MAX_VALUE,
                    Sort.by(Sort.Direction.DESC, "createdAt")))
            .getContent();

    byte[] csvData = auditLogExportService.exportToCsv(auditLogs);

    return ResponseEntity.ok()
            .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=audit-logs.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csvData);
}


@GetMapping("/export/csv/filter")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<byte[]> exportFilteredAuditLogsToCsv(
        @RequestParam String action,
        @RequestParam String status,
        @RequestParam
        @org.springframework.format.annotation.DateTimeFormat(
                iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startDate,
        @RequestParam
        @org.springframework.format.annotation.DateTimeFormat(
                iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endDate) throws IOException {

    Page<AuditLog> auditLogPage =
            auditLogService.getAuditLogsByFilters(
                    action,
                    status,
                    startDate,
                    endDate,
                    org.springframework.data.domain.PageRequest.of(
                            0, 10000,
                            Sort.by(Sort.Direction.DESC, "createdAt")));

    byte[] csvData =
            auditLogExportService.exportToCsv(auditLogPage.getContent());

    return ResponseEntity.ok()
            .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=filtered-audit-logs.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csvData);
}


@GetMapping("/export/csv/user/{username}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<byte[]> exportAuditLogsByUsername(
        @PathVariable String username) throws IOException {

    Page<AuditLog> auditLogPage =
            auditLogService.getAuditLogsByUsername(
                    username,
                    org.springframework.data.domain.PageRequest.of(
                            0, 10000,
                            Sort.by(Sort.Direction.DESC, "createdAt")));

    byte[] csvData =
            auditLogExportService.exportToCsv(
                    auditLogPage.getContent());

    return ResponseEntity.ok()
            .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=audit-logs-" + username + ".csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csvData);
}


@GetMapping("/export/csv/user/{username}/account/{accountNumber}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<byte[]> exportAuditLogsByUsernameAndAccount(
        @PathVariable String username,
        @PathVariable String accountNumber) throws IOException {

    Page<AuditLog> auditLogPage =
            auditLogService.getAuditLogsByUsernameAndAccount(
                    username,
                    accountNumber,
                    org.springframework.data.domain.PageRequest.of(
                            0, 10000,
                            Sort.by(Sort.Direction.DESC, "createdAt")));

    byte[] csvData =
            auditLogExportService.exportToCsv(
                    auditLogPage.getContent());

    return ResponseEntity.ok()
            .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=audit-logs-" + username + ".csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csvData);
}

}
