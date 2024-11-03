package com.sellsphere.provider.listener;



import com.sellsphere.provider.customer.external.Setting;

import java.util.List;

public class EmailSettingManager extends SettingManager {
    public EmailSettingManager(List<Setting> settings) {
        super(settings);
    }

    public String getHost() {
        return super.getSettingValue("MAIL_HOST");
    }

    public int getPort() {
        return Integer.parseInt(super.getSettingValue("MAIL_PORT"));
    }

    public String getUsername() {
        return super.getSettingValue("MAIL_USERNAME");
    }

    public String getPassword() {
        return super.getSettingValue("MAIL_PASSWORD");
    }

    /**
     * This property enables SMTP authentication, which is the process of the
     * client identifying itself to the server. Authentication is a critical
     * security feature that ensures only authorized users can send emails
     * through the SMTP server.
     */
    public boolean getEnableSmtpAuthentication() {
        return Boolean.parseBoolean(super.getSettingValue("SMTP_AUTH"));
    }

    /**
     * This property enables the use of STARTTLS, a command to upgrade an
     * existing insecure connection to a secure connection using TLS
     * (Transport Layer Security) before any login information or email data
     * is exchanged.
     * <p>
     * This property enforces that the connection must use STARTTLS and be
     * upgraded to a secure connection using TLS. If STARTTLS is not
     * available or fails, the connection will not proceed.
     */
    public boolean getEnableTLS() {
        return Boolean.parseBoolean(super.getSettingValue("SMTP_SECURED"));
    }

    public String getMailFrom() {
        return super.getSettingValue("MAIL_FROM");
    }

    public String getSenderName() {
        return super.getSettingValue("MAIL_SENDER_NAME");
    }

    public String getSubject() {
        return super.getSettingValue("CUSTOMER_EMAIL_VERIFY_SUBJECT");
    }

    public String getContent() {
        return super.getSettingValue("CUSTOMER_EMAIL_VERIFY_CONTENT");
    }

}
