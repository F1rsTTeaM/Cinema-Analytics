package com.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    private String reportName;
    private String reportType;
    private LocalDateTime generatedAt;
    private Map<String, Object> data;
    private List<ChartData> charts;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartData {
        private String type;
        private String title;
        private List<String> labels;
        private List<Number> values;
        private List<String> colors;
    }
}