package com.cinema.controller;

import com.cinema.dto.ReportDTO;
import com.cinema.service.ExportService;
import com.cinema.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ExportService exportService;

    @GetMapping("/tickets/summary")
    public ResponseEntity<ReportDTO> getTicketSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.getTicketSummaryReport(start, end));
    }

    @GetMapping("/tickets/movies")
    public ResponseEntity<ReportDTO> getTicketMovies(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.getMovieReport(start, end));
    }

    @GetMapping("/tickets/halls")
    public ResponseEntity<ReportDTO> getTicketHalls(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.getHallReport(start, end));
    }

    @GetMapping("/tickets/daily")
    public ResponseEntity<ReportDTO> getTicketDaily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.getDailyReport(start, end));
    }

    @GetMapping("/products/summary")
    public ResponseEntity<ReportDTO> getProductSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.getProductSummaryReport(start, end));
    }

    @GetMapping("/products/list")
    public ResponseEntity<ReportDTO> getProductList(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.getProductListReport(start, end));
    }

    @GetMapping("/products/daily")
    public ResponseEntity<ReportDTO> getProductDaily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.getProductDailyReport(start, end));
    }


    private ReportDTO getReportByType(String reportType, LocalDateTime start, LocalDateTime end) {
        switch (reportType) {
            case "tickets-summary":
                return reportService.getTicketSummaryReport(start, end);
            case "tickets-movies":
                return reportService.getMovieReport(start, end);
            case "tickets-halls":
                return reportService.getHallReport(start, end);
            case "tickets-daily":
                return reportService.getDailyReport(start, end);
            case "products-summary":
                return reportService.getProductSummaryReport(start, end);
            case "products-list":
                return reportService.getProductListReport(start, end);
            case "products-daily":
                return reportService.getProductDailyReport(start, end);
            default:
                throw new IllegalArgumentException("Unknown report type: " + reportType);
        }
    }

    @GetMapping("/export/{reportType}/{format}")
    public ResponseEntity<byte[]> exportReport(
            @PathVariable String reportType,
            @PathVariable String format,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        try {
            ReportDTO report = getReportByType(reportType, start, end);
            byte[] content;
            String fileName = reportType + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            MediaType mediaType;
            
            switch (format.toLowerCase()) {
                case "csv":
                    content = exportService.exportCSV(report);
                    fileName += ".csv";
                    mediaType = MediaType.parseMediaType("text/csv");
                    break;
                    
                case "json":
                    content = exportService.exportJSON(report);
                    fileName += ".json";
                    mediaType = MediaType.APPLICATION_JSON;
                    break;
                    
                case "pdf":
                    content = exportService.exportPDF(report);
                    fileName += ".pdf";
                    mediaType = MediaType.APPLICATION_PDF;
                    break;
                    
                default:
                    return ResponseEntity.badRequest().build();
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentDispositionFormData("attachment", fileName);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(content);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}