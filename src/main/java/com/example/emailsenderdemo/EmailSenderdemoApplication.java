package com.example.emailsenderdemo;

import java.sql.*;
import java.util.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class EmailSenderdemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailSenderdemoApplication.class, args);
    }
}

@Component
class EmailCampaignRunner implements CommandLineRunner {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPass;

    @Value("${spring.mail.host}")
    private String smtpHost;

    @Value("${spring.mail.port}")
    private int smtpPort;

    @Value("${spring.mail.username}")
    private String smtpUser;

    @Value("${spring.mail.password}")
    private String smtpPass;

    @Value("${email.campaign.delay-ms}")
    private int delayMsBetweenEmails;

    @Override
    public void run(String... args) throws Exception {
        sendCampaignEmails();
    }

    // Метод рассылки
    public void sendCampaignEmails() {
        String campaignId = "demo_campaign_001"; // менять для новых рассылок
        String subject = "Добро пожаловать в нашу рассылку!";
        String htmlFilePath = "src/main/resources/email_template.html";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
            List<String> recipients = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT email_address FROM email_subscribers " +
                            "WHERE is_subscribed = TRUE AND mailbox_exists = TRUE " +
                            "AND email_address NOT IN (" +
                            "  SELECT email_address FROM email_campaign_logs WHERE campaign_id = ?" +
                            ")")) {
                stmt.setString(1, campaignId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    recipients.add(rs.getString("email_address"));
                }
            }

            if (recipients.isEmpty()) {
                System.out.println("Нет адресов для рассылки.");
                return;
            }

            String htmlContent = Files.readString(new File(htmlFilePath).toPath());

            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", String.valueOf(smtpPort));
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPass);
                }
            });

            for (String recipient : recipients) {
                try {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(smtpUser));
                    message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
                    message.setSubject(subject);

                    String unsubscribeLink = "http://localhost:8080/api/unsubscribe?email=" + recipient;

                    String personalizedHtmlContent = htmlContent +
                            "<br><br><a href=\"" + unsubscribeLink +
                            "\" style=\"color: red;\">Unsubscribe</a>";

                    MimeBodyPart htmlPart = new MimeBodyPart();
                    htmlPart.setContent(personalizedHtmlContent, "text/html; charset=utf-8");

                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    attachmentPart.attachFile(new File(htmlFilePath));

                    Multipart multipart = new MimeMultipart();
                    multipart.addBodyPart(htmlPart);
                    multipart.addBodyPart(attachmentPart);

                    message.setContent(multipart);

                    Transport.send(message);
                    System.out.println("Отправлено: " + recipient);

                    try (PreparedStatement logStmt = conn.prepareStatement(
                            "INSERT INTO email_campaign_logs (email_address, campaign_id) VALUES (?, ?)")) {
                        logStmt.setString(1, recipient);
                        logStmt.setString(2, campaignId);
                        logStmt.executeUpdate();
                    }

                    Thread.sleep(delayMsBetweenEmails);
                } catch (Exception e) {
                    System.out.println("Ошибка при отправке на: " + recipient);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Метод отписки
    public boolean unsubscribeEmail(String email) {
        String updateSql = "UPDATE email_subscribers SET is_subscribed = FALSE WHERE email_address = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {

            stmt.setString(1, email);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Email " + email + " успешно отписан.");
                return true;
            } else {
                System.out.println("Email " + email + " не найден в базе.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
