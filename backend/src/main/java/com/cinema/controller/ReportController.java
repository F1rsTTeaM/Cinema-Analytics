package com.cinema.controller;

import com.cinema.dto.EmailReportRequest;
import com.cinema.dto.ReportDTO;
import com.cinema.dto.ReportTaskResponse;
import com.cinema.service.AsyncReportService;
import com.cinema.service.ExportService;
import com.cinema.service.ReportService;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ExportService exportService;

    @Autowired
    private AsyncReportService asyncReportService;

    @GetMapping("/tickets/summary")
    public ResponseEntity<ReportDTO> getTicketSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("GET /api/reports/tickets/summary - период: {} - {}", start, end);
        ReportDTO report = reportService.getTicketSummaryReport(start, end);
        log.info("GET /api/reports/tickets/summary - отчёт сформирован");

        return ResponseEntity.ok(report);
    }

    @GetMapping("/tickets/movies")
    public ResponseEntity<ReportDTO> getTicketMovies(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("GET /api/reports/tickets/movies - период: {} - {}", start, end);
        ReportDTO report = reportService.getMovieReport(start, end);
        log.info("GET /api/reports/tickets/movies - отчёт сформирован");

        return ResponseEntity.ok(report);
    }

    @GetMapping("/tickets/halls")
    public ResponseEntity<ReportDTO> getTicketHalls(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("GET /api/reports/tickets/halls - период: {} - {}", start, end);
        ReportDTO report = reportService.getHallReport(start, end);
        log.info("GET /api/reports/tickets/halls - отчёт сформирован");

        return ResponseEntity.ok(report);
    }

    @GetMapping("/tickets/daily")
    public ResponseEntity<ReportDTO> getTicketDaily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("GET /api/reports/tickets/daily - период: {} - {}", start, end);
        ReportDTO report = reportService.getDailyReport(start, end);
        log.info("GET /api/reports/tickets/daily - отчёт сформирован");

        return ResponseEntity.ok(report);
    }

    @GetMapping("/products/summary")
    public ResponseEntity<ReportDTO> getProductSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("GET /api/reports/products/summary - период: {} - {}", start, end);
        ReportDTO report = reportService.getProductSummaryReport(start, end);
        log.info("GET /api/reports/tickets/summary - отчёт сформирован");

        return ResponseEntity.ok(report);
    }

    @GetMapping("/products/list")
    public ResponseEntity<ReportDTO> getProductList(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("GET /api/reports/products/list - период: {} - {}", start, end);
        ReportDTO report = reportService.getProductListReport(start, end);
        log.info("GET /api/reports/tickets/list - отчёт сформирован");

        return ResponseEntity.ok(report);
    }

    @GetMapping("/products/daily")
    public ResponseEntity<ReportDTO> getProductDaily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("GET /api/reports/products/daily - период: {} - {}", start, end);
        ReportDTO report = reportService.getProductDailyReport(start, end);
        log.info("GET /api/reports/tickets/daily - отчёт сформирован");

        return ResponseEntity.ok(report);
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

        log.info("GET /api/reports/export/{}/{} - период: {} - {}",
                reportType, format, start, end);
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

            log.info("Экспорт отчёта: reportType={}, format={}, fileName={}, size={} байт",
                    reportType, format, fileName, content.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(content);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/send-email")
    public ResponseEntity<ReportTaskResponse> sendReportByEmail(@RequestBody EmailReportRequest request) {
        log.info("POST /api/reports/send-email - reportType={}, format={}, to='{}'",
                request.getReportType(), request.getFormat(), request.getToEmail());

        String taskId = asyncReportService.startEmailTask(request);

        log.info("Задача отправки письма создана: taskId={}", taskId);

        return ResponseEntity.accepted().body(
                new ReportTaskResponse(taskId, "PENDING", "Задача создана, отправка в процессе"));
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<ReportTaskResponse> getTaskStatus(@PathVariable String taskId) {
        return ResponseEntity.ok(asyncReportService.getTask(taskId));
    }
}