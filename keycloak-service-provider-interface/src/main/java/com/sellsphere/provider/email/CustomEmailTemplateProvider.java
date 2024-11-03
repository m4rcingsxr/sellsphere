package com.sellsphere.provider.email;

import com.sellsphere.provider.customer.external.Setting;
import com.sellsphere.provider.listener.EmailSettingManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.email.freemarker.FreeMarkerEmailTemplateProvider;
import org.keycloak.email.freemarker.beans.ProfileBean;
import org.keycloak.models.KeycloakSession;
import org.keycloak.theme.Theme;
import org.keycloak.theme.beans.MessageFormatterMethod;

import java.util.*;

public class CustomEmailTemplateProvider extends FreeMarkerEmailTemplateProvider {

    private final EntityManager entityManager;
    private final EmailSettingManager emailSettingManager;
    private final String siteName;

    /**
     * Constructor for CustomEmailTemplateProvider
     * Initializes EntityManager and loads email settings
     */
    public CustomEmailTemplateProvider(KeycloakSession session) {
        super(session);
        this.entityManager = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();
        this.emailSettingManager = loadEmailSettings();
        this.siteName = fetchSettingValue("SITE_NAME");
    }

    /**
     * Loads email-related settings from the database for use in the email template manager.
     */
    private EmailSettingManager loadEmailSettings() {
        String jpql = "SELECT s FROM Setting s WHERE s.category IN ('MAIL_TEMPLATES', 'MAIL_SERVER')";
        TypedQuery<Setting> query = entityManager.createQuery(jpql, Setting.class);
        List<Setting> settings = query.getResultList();
        return new EmailSettingManager(settings);
    }

    /**
     * Fetches a setting value from the database by key.
     */
    private String fetchSettingValue(String settingKey) {
        Setting setting = entityManager.find(Setting.class, settingKey);
        return (setting != null) ? setting.getValue() : "";
    }

    /**
     * Sends a verification email to the user with a custom subject and HTML body.
     */
    @Override
    public void sendVerifyEmail(String link, long expirationInMinutes) throws EmailException {
        Map<String, Object> attributes = new HashMap<>(this.attributes);

        // Set a 24-hour expiration for verification links
        addLinkInfoIntoAttributes(link, 24 * 60, attributes);

        // Process the email template and send only the HTML version of the email
        EmailTemplate email = processTemplate("emailVerificationSubject", Collections.emptyList(), "email-verification.ftl", attributes);
        sendEmailWithHtmlOnly(email.getSubject(), email.getHtmlBody());
    }

    /**
     * Sends an email with only the HTML body, skipping the plain text version.
     */
    private void sendEmailWithHtmlOnly(String subject, String htmlBody) throws EmailException {
        Map<String, String> smtpConfig = this.realm.getSmtpConfig();
        EmailSenderProvider emailSender = this.session.getProvider(EmailSenderProvider.class);
        emailSender.send(smtpConfig, this.user, subject, null, htmlBody);
    }

    /**
     * Processes the email template, replacing placeholders with dynamic values
     * like the link expiration, site name, and user's name.
     */
    @Override
    protected EmailTemplate processTemplate(String subjectKey, List<Object> subjectAttributes, String template, Map<String, Object> attributes) throws EmailException {
        try {
            Theme theme = this.getTheme();
            Locale locale = this.session.getContext().resolveLocale(this.user);
            attributes.put("locale", locale);
            attributes.put("realmName", this.getRealmName());
            attributes.put("user", new ProfileBean(this.user, this.session));

            // Set up localized messages for the email
            Properties messages = theme.getEnhancedMessages(this.realm, locale);
            attributes.put("msg", new MessageFormatterMethod(locale, messages));

            // Prepare the email subject using settings
            String subject = prepareSubject();

            // Convert expiration from minutes to hours for email display
            int linkExpirationHours = Integer.parseInt(attributes.get("linkExpiration").toString()) / 60;

            // Format the email body using dynamic values
            String templateBody = formatEmailBody(attributes, linkExpirationHours);

            // Add the processed HTML body content
            attributes.put("emailVerificationBodyHtml", templateBody);

            // Generate the HTML body using the template and attributes
            String htmlBody = this.freeMarker.processTemplate(attributes, "html/" + template, theme);

            return new EmailTemplate(subject, null, htmlBody);
        } catch (Exception e) {
            throw new EmailException("Failed to process email template.", e);
        }
    }

    /**
     * Prepares the subject line for the email by fetching and formatting it
     * with the site name.
     */
    private String prepareSubject() {
        String subjectTemplate = fetchSettingValue("CUSTOMER_EMAIL_VERIFY_SUBJECT");
        return subjectTemplate.replace("[[site_name]]", siteName);
    }

    /**
     * Formats the email body by replacing dynamic placeholders like the user's
     * first name, site name, verification link, and expiration time.
     */
    private String formatEmailBody(Map<String, Object> attributes, int linkExpirationHours) {
        String templateBody = emailSettingManager.getContent();
        templateBody = templateBody.replace("[[customer_first_name]]", this.user.getFirstName())
                .replace("[[site_name]]", siteName)
                .replace("[[verification_link]]", attributes.get("link").toString())
                .replace("[[verification_link_expiration]]", String.valueOf(linkExpirationHours));
        return templateBody;
    }
}
