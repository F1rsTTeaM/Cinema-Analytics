package com.cinema.service;

import com.cinema.dto.EmailReportRequest;
import com.cinema.dto.ReportTaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class AsyncReportService {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ExportService exportService;

    @Autowired
    private EmailService emailService;

    private final Map<String, ReportTaskResponse> tasks = new ConcurrentHashMap<>();

    public String startEmailTask(EmailReportRequest request) {
        String taskId = UUID.randomUUID().toString();

        tasks.put(taskId, new ReportTaskResponse(
                taskId, "PENDING", "Задача создана, ожидает выполнения"));

        processEmailTask(taskId, request);

        return taskId;
    }

    public ReportTaskResponse getTask(String taskId) {
        ReportTaskResponse task = tasks.get(taskId);
        if (task == null) {
            return new ReportTaskResponse(taskId, "NOT_FOUND", "Задача не найдена");
        }
        return task;
    }

    @Async
    public void processEmailTask(String taskId, EmailReportRequest request) {
        updateTask(taskId, "IN_PROGRESS", "Формирование отчёта...");

        try {
            var report = reportService.getReportByType(
                    request.getReportType(),
                    request.getStartDate(),
                    request.getEndDate());

            byte[] content;
            String fileName;
            String contentType;
            String baseName = request.getReportType() + "_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            switch (request.getFormat().toLowerCase()) {
                case "csv" -> {
                    content = exportService.exportCSV(report);
                    fileName = baseName + ".csv";
                    contentType = "text/csv";
                }
                case "json" -> {
                    content = exportService.exportJSON(report);
                    fileName = baseName + ".json";
                    contentType = "application/json";
                }
                case "pdf" -> {
                    content = exportService.exportPDF(report);
                    fileName = baseName + ".pdf";
                    contentType = "application/pdf";
                }
                default -> throw new IllegalArgumentException(
                        "Неизвестный формат: " + request.getFormat());
            }

            updateTask(taskId, "IN_PROGRESS", "Отправка письма...");

            String period = request.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                    + " - "
                    + request.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

            emailService.sendReportEmail(
                    request.getToEmail(),
                    request.getSubject(),
                    request.getMessage(),
                    fileName,
                    content,
                    contentType,
                    report.getReportName(),
                    period);

            updateTask(taskId, "SUCCESS",
                    "✔️ Отчет успешно отправлен на " + request.getToEmail());

            log.info("Фоновая задача {} завершена успешно", taskId);

        } catch (Exception e) {
            log.error("Фоновая задача {} упала: {}", taskId, e.getMessage(), e);
            updateTask(taskId, "FAILED",
                    "❌ Ошибка отправки: " + e.getMessage());
        }
    }

    private void updateTask(String taskId, String status, String message) {
        ReportTaskResponse task = tasks.get(taskId);
        if (task != null) {
            task.setStatus(status);
            task.setMessage(message);
        }
    }
}