package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailReportRequest {
    private String toEmail;
    private String reportType;
    private String format;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String subject;
    private String message;
}