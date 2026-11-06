package com.infy.bankingApplication.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.infy.bankingApplication.entity.AuditLog;

@Service
public class AuditLogExportService {
	
	private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public byte[] exportToCsv(List<AuditLog> auditLogs)
            throws IOException {

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        try (OutputStreamWriter writer =
                new OutputStreamWriter(
                        outputStream,
                        StandardCharsets.UTF_8)) {

            writer.write(
                    "ID,Username,Action,Account Number,"
                    + "Target Account,Amount,Status,Created At\n");

            for (AuditLog log : auditLogs) {
                writeRow(writer,
                        log.getId(),
                        log.getUsername(),
                        log.getAction(),
                        log.getAccountNumber(),
                        log.getTargetAccount(),
                        log.getAmount(),
                        log.getStatus(),
                        log.getCreatedAt() == null
                                ? null
                                : log.getCreatedAt()
                                        .format(DATE_FORMAT));
            }
        }

        return outputStream.toByteArray();
    }

    private void writeRow(
            OutputStreamWriter writer,
            Object... values) throws IOException {

        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                writer.write(",");
            }

            writer.write(escapeCsv(
                    values[i] == null
                            ? ""
                            : values[i].toString()));
        }

        writer.write("\n");
    }

    private String escapeCsv(String value) {
        String escaped = value.replace("\"", "\"\"");

        return "\"" + escaped + "\"";
    }

}
