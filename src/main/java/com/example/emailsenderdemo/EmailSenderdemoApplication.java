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

@SpringBootApplication
public class EmailSenderdemoApplication {

    // Константы на уровне класса
    static final String DB_URL = "jdbc:postgresql://localhost:5432/emaildb";
    static final String DB_USER = "postgres";
    static final String DB_PASS = "Shorlotik003";

    static final String SMTP_HOST = "mail.gemtoo.dev";
    static final int SMTP_PORT = 465;
    static final String SMTP_USER = "m.prokopchik@gemtoo.dev";
    static final String SMTP_PASS = "HhdSMdTOaH2qEki1YDojLZIxKtqFZWKq";

    // лимит: 200 сообщений в минуту = 1 сообщение каждые 300 мс
    static final int DELAY_MS_BETWEEN_EMAILS = 300;

    public static void main(String[] args) {
        SpringApplication.run(EmailSenderdemoApplication.class, args);

        sendCampaignEmails();
    }

    // Метод рассылки
    public static void sendCampaignEmails() {
        String campaignId = "ezeewallet86"; // менять для новых рассылок
        String subject = "Introducing eZeeWallet: Simplify your payments!";
        String htmlFilePath = "src/main/resources/Introducing eZeeWallet.html";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
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
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
                }
            });

            for (String recipient : recipients) {
                try {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(SMTP_USER));
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

                    Thread.sleep(DELAY_MS_BETWEEN_EMAILS);
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
    public static boolean unsubscribeEmail(String email) {
        String updateSql = "UPDATE email_subscribers SET is_subscribed = FALSE WHERE email_address = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
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
