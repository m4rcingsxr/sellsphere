package com.sellsphere.client;

import com.sellsphere.client.setting.SettingService;
import com.sellsphere.common.entity.Order;
import com.sellsphere.common.entity.OrderDetail;
import com.sellsphere.common.entity.Setting;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

@Service
@Log4j2
@RequiredArgsConstructor
public class EmailService {

    private final SettingService settingService;


    public void sendOrderConfirmationEmail(Order order) {
        List<Setting> emailSettings = settingService.getEmailSettings();

        JavaMailSenderImpl mailSender = setupMailSender(emailSettings);

        String siteName = settingService.getByKey("SITE_NAME");
        String subject = settingService.getByKey("ORDER_CONFIRMATION_SUBJECT");
        String content = settingService.getByKey("ORDER_CONFIRMATION_CONTENT");

        // Replace placeholders in subject and content
        subject = subject.replace("[[site_name]]", siteName);

        content = content
                .replace("[[customer_first_name]]", order.getTransaction().getCustomer().getFirstName())
                .replace("[[site_name]]", siteName)
                .replace("[[order_number]]", order.getId().toString())
                .replace("[[order_date]]", order.getOrderTime().toString())
                .replace("[[order_total]]", order.getTransaction().getDisplayAmountString())
                .replace("[[shipping_address]]", order.getTransaction().getShippingAddress().getFullAddress())
                .replace("[[estimated_delivery_days]]", order.getEstimatedDeliveryDate().toString())
                .replace("[[receipt_url]]", order.getTransaction().getReceiptUrl());

        // Build order details as HTML
        StringBuilder builder = new StringBuilder();
        builder.append("<ul>");
        String currencyCode = order.getTransaction().getTargetCurrency().getCode();

        for (OrderDetail orderDetail : order.getOrderDetails()) {
            builder.append("<li class=\"mt-2 p-2\">");
            builder.append("<div class=\"d-flex flex-column gap-2\">");
            builder.append("<img class=\"img-fluid\" style=\"max-height: 200px;\" src=\"").append(orderDetail.getProduct().getMainImagePath()).append("\"/>");
            builder.append("<p>Name: ").append(orderDetail.getProduct().getName()).append("</p>");
            builder.append("<p>Price: ").append(orderDetail.getProduct().getDiscountPrice()).append(currencyCode).append("</p>");
            builder.append("</div>");
            builder.append("</li>");
        }
        builder.append("</ul>");

        // Append order details to content
        content = content.replace("[[order]]", builder.toString());

        // Prepare MIME message for HTML content
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(order.getTransaction().getCustomer().getEmail());
            helper.setSubject(subject);
            helper.setText(content, true);  // Set to true for HTML content
            helper.setFrom(mailSender.getJavaMailProperties().get("mail.from").toString());

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }


    private JavaMailSenderImpl setupMailSender(List<Setting> emailSettings) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        Properties props = new Properties();

        for (Setting emailSetting : emailSettings) {
            switch (emailSetting.getKey()) {
                case "MAIL_HOST" -> mailSender.setHost(emailSetting.getValue());
                case "MAIL_PORT" -> mailSender.setPort(Integer.parseInt(emailSetting.getValue()));
                case "MAIL_USERNAME" -> mailSender.setUsername(emailSetting.getValue());
                case "MAIL_PASSWORD" -> mailSender.setPassword(emailSetting.getValue());
                case "SMTP_AUTH" -> props.put("mail.smtp.auth", emailSetting.getValue());
                case "SMTP_SECURED" -> props.put("mail.smtp.starttls.enable", emailSetting.getValue());
                case "MAIL_FROM" -> props.put("mail.from", emailSetting.getValue());
                case "MAIL_SENDER_NAME" -> props.put("mail.from.name", emailSetting.getValue());
            }
        }

        mailSender.setJavaMailProperties(props);
        return mailSender;
    }

}
