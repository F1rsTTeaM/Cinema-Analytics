package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    private String reportType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String format;
}