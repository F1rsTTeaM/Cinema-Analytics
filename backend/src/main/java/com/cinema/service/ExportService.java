package com.cinema.service;

import com.cinema.dto.ReportDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public byte[] exportCSV(ReportDTO report) {
        StringBuilder csv = new StringBuilder();

        String formattedDate = report.getGeneratedAt().format(DATE_FORMATTER);
        csv.append("Отчет: ").append(report.getReportName()).append("\n");
        csv.append("Сгенерирован: ").append(formattedDate).append("\n\n");

        if (report.getData() != null) {
            List<String> keys = new ArrayList<>();
            List<String> values = new ArrayList<>();
            int maxKeyLength = 0;

            for (Map.Entry<String, Object> entry : report.getData().entrySet()) {
                String key = translateKey(entry.getKey());
                Object value = entry.getValue();

                if (!(value instanceof List)) {
                    String displayValue = "";
                    if (value instanceof BigDecimal) {
                        displayValue = String.format("%.2f", ((BigDecimal) value).doubleValue());
                    } else {
                        displayValue = value != null ? value.toString() : "";
                    }
                    keys.add(key);
                    values.add(displayValue);
                    maxKeyLength = Math.max(maxKeyLength, key.length());
                }
            }

            for (int i = 0; i < keys.size(); i++) {
                String paddedKey = String.format("%-" + (maxKeyLength + 2) + "s", keys.get(i));
                csv.append(paddedKey).append(";").append(values.get(i)).append("\n");
            }
            csv.append("\n");

            for (Map.Entry<String, Object> entry : report.getData().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    if (!list.isEmpty() && list.get(0) instanceof Map) {
                        Map<?, ?> first = (Map<?, ?>) list.get(0);
                        csv.append(translateKey(key)).append(":\n");
                        csv.append(String.join(";", first.keySet().stream()
                                .map(k -> translateKey(k.toString()))
                                .toArray(String[]::new))).append("\n");

                        for (Object item : list) {
                            Map<?, ?> map = (Map<?, ?>) item;
                            csv.append(String.join(";", map.values().stream()
                                    .map(v -> {
                                        if (v == null)
                                            return "";
                                        if (v instanceof BigDecimal) {
                                            return String.format("%.2f", ((BigDecimal) v).doubleValue());
                                        }
                                        return v.toString();
                                    })
                                    .toArray(String[]::new))).append("\n");
                        }
                        csv.append("\n");
                    }
                }
            }
        }

        byte[] content = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

        byte[] result = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(content, 0, result, bom.length, content.length);

        return result;
    }

    public byte[] exportJSON(ReportDTO report) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(report);
    }

    

    public byte[] exportPDF(ReportDTO report) throws DocumentException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        document.open();

        BaseFont baseFont;
        try {
            baseFont = BaseFont.createFont(
                    "/fonts/DejaVuSans.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED);
        } catch (Exception e) {
            try {
                baseFont = BaseFont.createFont(
                        "c:/windows/fonts/arial.ttf",
                        BaseFont.IDENTITY_H,
                        BaseFont.EMBEDDED);
            } catch (Exception e2) {
                baseFont = BaseFont.createFont(
                        BaseFont.HELVETICA,
                        BaseFont.CP1252,
                        BaseFont.NOT_EMBEDDED);
            }
        }

        Font titleFont = new Font(baseFont, 18, Font.BOLD);
        Font headerFont = new Font(baseFont, 14, Font.BOLD);
        Font normalFont = new Font(baseFont, 12, Font.NORMAL);
        Font boldFont = new Font(baseFont, 12, Font.BOLD);
        Font smallBoldFont = new Font(baseFont, 10, Font.BOLD);

        Paragraph title = new Paragraph("Отчет: " + report.getReportName(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        String formattedDate = report.getGeneratedAt().format(DATE_FORMATTER);
        Paragraph date = new Paragraph("Сгенерирован: " + formattedDate, normalFont);
        date.setAlignment(Element.ALIGN_CENTER);
        document.add(date);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        if (report.getData() != null && !report.getData().isEmpty()) {
            List<Map<String, String>> simpleData = new ArrayList<>();

            for (Map.Entry<String, Object> entry : report.getData().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    if (!list.isEmpty() && list.get(0) instanceof Map) {
                        document.add(new Paragraph(" "));
                        Paragraph sectionHeader = new Paragraph(translateKey(key), headerFont);
                        document.add(sectionHeader);

                        Map<?, ?> first = (Map<?, ?>) list.get(0);
                        PdfPTable table = new PdfPTable(first.keySet().size());
                        table.setWidthPercentage(100);

                        for (Object colKey : first.keySet()) {
                            String headerText = translateKey(colKey.toString());
                            PdfPCell cell = new PdfPCell(new Phrase(headerText, boldFont));
                            cell.setBackgroundColor(new BaseColor(200, 200, 200));
                            cell.setPadding(5);
                            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            table.addCell(cell);
                        }

                        for (Object item : list) {
                            Map<?, ?> map = (Map<?, ?>) item;
                            for (Object colKey : first.keySet()) {
                                Object val = map.get(colKey);
                                String text = val != null ? val.toString() : "";
                                if (val instanceof BigDecimal) {
                                    text = String.format("%.2f", ((BigDecimal) val).doubleValue());
                                }
                                PdfPCell cell = new PdfPCell(new Phrase(text, normalFont));
                                cell.setPadding(4);
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                table.addCell(cell);
                            }
                        }

                        document.add(table);
                        document.add(new Paragraph(" "));
                    }
                } else {
                    Map<String, String> row = new LinkedHashMap<>();
                    String displayValue = "—";
                    if (value != null && !value.toString().trim().isEmpty()) {
                        displayValue = value.toString();
                        if (value instanceof BigDecimal) {
                            displayValue = String.format("%.2f", ((BigDecimal) value).doubleValue());
                        }
                    }
                    row.put("key", translateKey(key));
                    row.put("value", displayValue);
                    simpleData.add(row);
                }
            }

            if (!simpleData.isEmpty()) {
                PdfPTable simpleTable = new PdfPTable(2);
                simpleTable.setWidthPercentage(100);
                simpleTable.setWidths(new float[] { 40, 60 });

                PdfPCell headerCell1 = new PdfPCell(new Phrase("Показатель", boldFont));
                headerCell1.setBackgroundColor(new BaseColor(200, 200, 200));
                headerCell1.setPadding(5);
                headerCell1.setHorizontalAlignment(Element.ALIGN_CENTER);
                simpleTable.addCell(headerCell1);

                PdfPCell headerCell2 = new PdfPCell(new Phrase("Значение", boldFont));
                headerCell2.setBackgroundColor(new BaseColor(200, 200, 200));
                headerCell2.setPadding(5);
                headerCell2.setHorizontalAlignment(Element.ALIGN_CENTER);
                simpleTable.addCell(headerCell2);

                for (Map<String, String> row : simpleData) {
                    PdfPCell keyCell = new PdfPCell(new Phrase(row.get("key"), smallBoldFont));
                    keyCell.setPadding(4);
                    keyCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    simpleTable.addCell(keyCell);

                    PdfPCell valueCell = new PdfPCell(new Phrase(row.get("value"), normalFont));
                    valueCell.setPadding(4);
                    valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    simpleTable.addCell(valueCell);
                }

                document.add(simpleTable);
                document.add(new Paragraph(" "));
            }
        } else {
            document.add(new Paragraph("Нет данных", normalFont));
        }

        document.close();
        return out.toByteArray();
    }

    private String translateKey(String key) {
        Map<String, String> translations = new HashMap<>();
        translations.put("topHallTickets", "Билетов у топ зала");
        translations.put("totalSessions", "Всего сеансов");
        translations.put("period", "Период");
        translations.put("topMovie", "Самый популярный фильм");
        translations.put("topHall", "Самый популярный зал");
        translations.put("totalTickets", "Всего билетов");
        translations.put("totalRevenue", "Общая выручка");
        translations.put("topMovieTickets", "Билетов у топ фильма");

        translations.put("movieStats", "Статистика по фильмам");
        translations.put("movieTitle", "Название фильма");
        translations.put("genre", "Жанр");
        translations.put("sessionsCount", "Количество сеансов");
        translations.put("ticketsSold", "Продано билетов"); 
        translations.put("totalMovies", "Всего фильмов");
        
        translations.put("hallStats", "Статистика по залам");
        translations.put("hallName", "Название зала");
        translations.put("capacity", "Вместимость");
        translations.put("occupancyPercent", "Заполняемость %");
        translations.put("totalHalls", "Всего залов");
        
        
        translations.put("date", "Дата");
        translations.put("daysCount", "Количество дней");

        translations.put("totalItems", "Всего товаров");
        translations.put("topProductCount", "Продаж топ продукта");
        translations.put("topProduct", "Топ продукт");

        translations.put("productStats", "Статистика по товарам");
        translations.put("productName", "Название товара");
        translations.put("totalSold", "Продано");
        translations.put("totalProducts", "Всего товаров");

        translations.put("dailyStats", "Статистика по дням");
        translations.put("itemsSold", "Продано товаров");
        translations.put("revenue", "Выручка");

        return translations.getOrDefault(key, key);
    }
}