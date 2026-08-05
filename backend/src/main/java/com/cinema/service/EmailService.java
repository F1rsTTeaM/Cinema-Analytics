package com.cinema.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    public void sendReportEmail(
            String toEmail,
            String subject,
            String messageText,
            String fileName,
            byte[] attachment,
            String contentType,
            String reportName,
            String period) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject != null ? subject : "Отчет: " + reportName);

        String htmlContent = buildEmailContent(reportName, period, messageText);
        helper.setText(htmlContent, true);

        helper.addAttachment(fileName, new ByteArrayResource(attachment) {
            @Override
            public String getFilename() {
                return fileName;
            }
        });

        mailSender.send(message);
    }

    public void sendReportEmailWithMultipleAttachments(
            String toEmail,
            String subject,
            String messageText,
            Map<String, byte[]> attachments,
            Map<String, String> contentTypes,
            String reportName,
            String period) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject != null ? subject : "Отчет: " + reportName);

        String htmlContent = buildEmailContent(reportName, period, messageText);
        helper.setText(htmlContent, true);

        for (Map.Entry<String, byte[]> entry : attachments.entrySet()) {
            String fileName = entry.getKey();
            byte[] content = entry.getValue();
            helper.addAttachment(fileName, new ByteArrayResource(content) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            });
        }

        mailSender.send(message);
    }

    private String buildEmailContent(String reportName, String period, String customMessage) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; color: #333; }");
        html.append(".header { background: #667eea; color: white; padding: 20px; text-align: center; }");
        html.append(".content { padding: 20px; }");
        html.append(".footer { background: #f5f5f5; padding: 10px; text-align: center; font-size: 12px; color: #888; }");
        html.append(".info { background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 15px 0; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class='header'>");
        html.append("<h2>Отчет сгенерирован</h2>");
        html.append("</div>");
        
        html.append("<div class='content'>");
        html.append("<h3>").append(reportName).append("</h3>");
        html.append("<div class='info'>");
        html.append("<p><strong>Период:</strong> ").append(period).append("</p>");
        if (customMessage != null && !customMessage.isEmpty()) {
            html.append("<p><strong>Сообщение:</strong> ").append(customMessage).append("</p>");
        }
        html.append("<p><strong>Файл отчета прикреплен к письму</strong></p>");
        html.append("</div>");
        html.append("</div>");
        
        html.append("<div class='footer'>");
        html.append("<p>Это письмо сгенерировано автоматически системой Cinema Analytics</p>");
        html.append("</div>");
        
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    private static class ByteArrayResource extends org.springframework.core.io.ByteArrayResource {
        public ByteArrayResource(byte[] byteArray) {
            super(byteArray);
        }

        @Override
        public String getFilename() {
            return super.getFilename();
        }
    }
}